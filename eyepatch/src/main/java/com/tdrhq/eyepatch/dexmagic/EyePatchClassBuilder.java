package com.tdrhq.eyepatch.dexmagic;

import android.support.annotation.NonNull;
import android.util.Log;
import com.android.dx.*;
import com.tdrhq.eyepatch.util.Checks;
import dalvik.system.DexFile;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EyePatchClassBuilder {
    private File mDataDir;
    private int counter = 0;
    private Map<Key, DexFile> cache = new HashMap<>();

    public EyePatchClassBuilder(File dataDir) {
        mDataDir = dataDir;
    }

    /**
     * Wraps realClass, to generate a patchable class and loads it
     * into the ClassLoader.
     */
    public Class wrapClass(Class realClass, ClassLoader classLoader) {
        if (realClass.getClassLoader() == classLoader) {
            throw new IllegalArgumentException(
                    "The classLoader provided must be different from the one " +
                    "used to load realClass");
        }
        DexFile dexFile = generateDexFile(realClass, classLoader);
        return dexFile.loadClass(realClass.getName(), classLoader);

    }

    @NonNull
    DexFile generateDexFile(Class realClass, ClassLoader classLoader) {
        Key key = new Key(realClass, classLoader);
        if (cache.containsKey(key)) {
            return cache.get(key);
        } else {
            DexFile ret = generateDexFileUncached(realClass);
            cache.put(key, ret);
            return ret;
        }
    }

    @NonNull
    DexFile generateDexFileUncached(Class realClass) {
        DexMaker dexmaker = buildDexMaker(realClass.getName(), realClass);
        try {
            File of = new File(mDataDir, "EPG" + (++counter) + ".jar");
            return Util.createDexFile(dexmaker, of);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private DexMaker buildDexMaker(String name, Class original) {
        DexMaker dexmaker = new DexMaker();
        TypeId<?> typeId = Util.createTypeIdForName(name);
        dexmaker.declare(typeId, name + ".generated", Modifier.PUBLIC, TypeId.get(original.getSuperclass()));

        for (Field field : original.getDeclaredFields()) {
            generateField(dexmaker, field, typeId);
        }

        for (Constructor constructor : original.getDeclaredConstructors()) {
            generateConstructor(dexmaker, constructor, typeId, original);
        }

        for (Method methodTemplate : original.getDeclaredMethods()) {
            generateMethod(dexmaker, methodTemplate, typeId, original);
        }
        return dexmaker;
    }

    private static void generateField(DexMaker dexmaker, Field field, TypeId<?> typeId) {
        FieldId<?, ?> fieldId = typeId.getField(
                TypeId.get(field.getType()),
                field.getName());
        int modifiers = field.getModifiers();
        dexmaker.declare(fieldId, modifiers, null);
    }

    private static void generateConstructor(DexMaker dexmaker, Constructor constructor, final TypeId<?> typeId, Class original) {
        String methodName = "__construct__";
        int modifiers = constructor.getModifiers();
        TypeId returnType = TypeId.VOID;
        Class[] parameterTypes = constructor.getParameterTypes();
        TypeId[] arguments = new TypeId[parameterTypes.length];
        for (int i = 0 ;i < parameterTypes.length; i++) {
            arguments[i] = TypeId.get(parameterTypes[i]);
        }
        MethodId cons = typeId.getConstructor(arguments);
        TypeId parent = TypeId.get(original.getSuperclass());
        Code  code = dexmaker.declare(cons, Modifier.PUBLIC);
        Locals locals = new Locals(code, returnType);

        invokeEasiestSuper(typeId, parent, original, code);

        generateMethodContentsInternal(code, typeId, returnType, parameterTypes, original, modifiers, methodName, locals);
    }

    private static void invokeEasiestSuper(TypeId<?> typeId, TypeId parent, Class original, Code code) {
        // Since this is the first method, we can still create locals
        Constructor parentConstructor = getEasiestConstructor(original.getSuperclass());
        Class[] parameterTypes = parentConstructor.getParameterTypes();
        TypeId[] argTypes = new TypeId[parameterTypes.length];
        Local[] parentArgs = new Local[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            argTypes[i] = TypeId.get(parameterTypes[i]);
            parentArgs[i] = code.newLocal(argTypes[i]);
        }

        for (int i = 0; i < parameterTypes.length; i++) {
            code.loadConstant(parentArgs[i], null);
        }

        code.invokeDirect(Checks.notNull(parent.getConstructor(argTypes)),
                          null, code.getThis(typeId), parentArgs);
    }

    private static Constructor getEasiestConstructor(Class klass) {
        Constructor best = null;
        int bestCost = Integer.MAX_VALUE;
        for (Constructor cons : klass.getDeclaredConstructors()) {
            if (getConstructorCost(cons) < bestCost) {
                best = cons;
            }
        }
        return best;
    }

    private static int getConstructorCost(Constructor cons) {
        return cons.getParameterTypes().length;
    }

    private void generateMethod(DexMaker dexmaker, Method methodTemplate, TypeId<?> typeId, Class original) {
        String methodName = methodTemplate.getName();
        int modifiers = methodTemplate.getModifiers();
        TypeId returnType = TypeId.get(methodTemplate.getReturnType());
        Class[] parameterTypes = methodTemplate.getParameterTypes();
        TypeId[] arguments = new TypeId[parameterTypes.length];
        for (int i = 0 ;i < parameterTypes.length; i++) {
            arguments[i] = TypeId.get(parameterTypes[i]);
        }
        MethodId foo = typeId.getMethod(returnType, methodName, arguments);
        Code code = dexmaker.declare(foo, modifiers);
        Locals locals = new Locals(code, returnType);

        generateMethodContentsInternal(code, typeId, returnType, parameterTypes, original, modifiers, methodName, locals);
    }

    private static void generateMethodContentsInternal(Code code, TypeId typeId, TypeId returnType, Class[] parameterTypes, Class original, int modifiers, String methodName, Locals locals) {
        TypeId staticInvoker = TypeId.get(StaticInvocationHandler.class);
        TypeId classType = TypeId.get(Class.class);
        TypeId instance = TypeId.OBJECT;
        TypeId objectType = TypeId.get(Object.class);
        TypeId stringType = TypeId.get(String.class);
        TypeId argArType = TypeId.get(Class[].class);
        TypeId objectArType = TypeId.get(Object[].class);

        MethodId invokeStaticMethod = staticInvoker.getMethod(
                objectType,
                "invokeStatic",
                classType,
                objectType,
                stringType,
                argArType,
                objectArType);


        code.loadConstant(locals.parameterLength, parameterTypes.length);
        code.loadConstant(locals.argTypes, null);

        buildCallerArray(locals.callerArgs, locals.parameterLength, locals.tmp, parameterTypes, code);
        code.loadConstant(locals.parameterLength, parameterTypes.length);
        buildArgArray(locals.argTypes, locals.parameterLength, parameterTypes, locals.tmp, code);

        code.loadConstant(locals.callerClass, original);
        if (Modifier.isStatic(modifiers)) {
            code.loadConstant(locals.instanceArg, null);
        } else {
            locals.instanceArg = code.getThis(typeId);
        }
        code.loadConstant(locals.callerMethod, methodName);
        code.invokeStatic(
                invokeStaticMethod,
                locals.returnValue,
                locals.callerClass,
                locals.instanceArg,
                locals.callerMethod,
                locals.argTypes,
                locals.callerArgs);

        if (Primitives.isPrimitive(returnType)) {
            MethodId intValue = Primitives.getBoxedType(returnType)
                    .getMethod(
                            returnType,
                            Primitives.getUnboxFunction(returnType));
            code.cast(locals.boxedReturnValue, locals.returnValue);
            code.invokeVirtual(
                    intValue,
                    locals.castedReturnValue,
                    locals.boxedReturnValue);

        } else if (returnType != TypeId.VOID) {
            code.cast(locals.castedReturnValue, locals.returnValue);
        }

        if (returnType == TypeId.VOID) {
            code.returnVoid();
        } else {
            code.returnValue(locals.castedReturnValue);
        }
    }

    private static class Locals {
        Local<Object> returnValue;
        Local<Class> callerClass;
        Local instanceArg;
        Local<String> callerMethod;
        Local<Class[]> argTypes;
        Local<Object[]> callerArgs;
        Local castedReturnValue;
        Local<Integer> parameterLength;
        Local<Object> tmp;
        Local boxedReturnValue;

        public Locals(Code code, TypeId returnType) {
            returnValue = null;

            if (returnType != TypeId.VOID) {
                returnValue = code.newLocal(TypeId.OBJECT);
            }

            callerClass = code.newLocal(TypeId.get(Class.class));
            instanceArg = code.newLocal(TypeId.OBJECT);
            callerMethod = code.newLocal(TypeId.STRING);
            argTypes = code.newLocal(TypeId.get(Class[].class));
            callerArgs = code.newLocal(TypeId.get(Object[].class));
            castedReturnValue = code.newLocal(returnType);
            parameterLength = code.newLocal(TypeId.INT);
            tmp = code.newLocal(TypeId.OBJECT);

            boxedReturnValue = null;

            if (Primitives.isPrimitive(returnType) && returnType != TypeId.VOID) {
                boxedReturnValue = code.newLocal(Primitives.getBoxedType(returnType));
            }

        }
    }

    private static void buildArgArray(
            Local<Class[]> output,
            Local<Integer> parameterLength,
            Class[] parameterTypes,
            Local<Object> tmp,
            Code code) {
        code.newArray(output, parameterLength);
        for (int i = 0; i < parameterTypes.length; i++) {
            code.loadConstant(parameterLength, i);
            code.loadConstant(tmp, parameterTypes[i]);
            code.aput(output, parameterLength, tmp);
        }
    }

    private static void buildCallerArray(
            Local<Object[]> callerArgs, Local<Integer> parameterLength,
            Local<Object> tmp,
            Class[] parameterTypes, Code code) {
        code.newArray(callerArgs, parameterLength);
        Log.i("EyePatchClassBuilder", "class: " + Arrays.toString(parameterTypes));
        for (int i = parameterTypes.length - 1; i>= 0; i--) {
            code.loadConstant(parameterLength, i);
            if (Primitives.isPrimitive(parameterTypes[i])) {
                code.newInstance(
                        tmp,
                        Primitives.getBoxedType(TypeId.get(parameterTypes[i])).getConstructor(
                                TypeId.get(parameterTypes[i])),
                        code.getParameter(i, TypeId.get(parameterTypes[i])));
            } else {
                code.cast(tmp, code.getParameter(i, TypeId.get(parameterTypes[i])));
            }
            code.aput(callerArgs, parameterLength, tmp);
        }
    }

    public static void invokeHelper(Class klass, String name) {
    }

    public static class Key {
        Class klass;
        ClassLoader classLoader;

        public Key(Class klass, ClassLoader classLoader) {
            this.klass = Checks.notNull(klass);
            this.classLoader = Checks.notNull(classLoader);
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Key)) {
                return false;
            }

            Key otherKey = (Key) other;
            return klass == otherKey.klass &&
                    classLoader == otherKey.classLoader;
        }

        @Override
        public int hashCode() {
            return klass.hashCode();
        }
    }
}
