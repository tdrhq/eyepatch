// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import android.support.annotation.NonNull;
import com.android.dx.*;
import com.tdrhq.eyepatch.util.Checks;
import com.tdrhq.eyepatch.util.ClassLoaderIntrospector;
import com.tdrhq.eyepatch.util.Whitebox;
import dalvik.system.DexFile;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public class DexFileGenerator {

    public static int UNUSED_REGISTER_COUNT = 256;
    private File mDataDir;
    private int counter = 0;
    private ConstructorGeneratorFactory constructorGeneratorFactory;
    private Merger merger = new Merger();

    public DexFileGenerator(File dataDir,
                            ConstructorGeneratorFactory mConstructorGeneratorFactory) {
        mDataDir = dataDir;
        constructorGeneratorFactory = mConstructorGeneratorFactory;
    }

    @NonNull
    public DexFile generate(Class realClass) {
        DexMaker dexmaker = buildDexMaker(realClass.getName(), realClass);
        try {
            int suffix = (++counter);
            File mergedOf = new File(mDataDir, "EPG_merged" + suffix + ".dex");

            ByteArrayInputStream template = new ByteArrayInputStream(dexmaker.generate());
            FileInputStream real = new FileInputStream(ClassLoaderIntrospector.getDefiningDexFile(realClass));

            try {
                merger.mergeDexFile(
                        template,
                        real,
                        mergedOf);
            } finally {
                template.close();
                real.close();
            }
            return Util.loadDexFile(mergedOf);
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

    private void generateConstructor(DexMaker dexmaker, Constructor constructor, final TypeId<?> typeId, Class original) {
        String methodName = EyePatchClassBuilder.CONSTRUCT;
        int modifiers = constructor.getModifiers();
        TypeId returnType = TypeId.VOID;
        Class[] parameterTypes = constructor.getParameterTypes();
        TypeId[] arguments = new TypeId[parameterTypes.length];
        for (int i = 0 ;i < parameterTypes.length; i++) {
            arguments[i] = TypeId.get(parameterTypes[i]);
        }
        MethodId cons = typeId.getConstructor(arguments);
        Code  code = dexmaker.declare(cons, Modifier.PUBLIC);
        Locals locals = new Locals(code, returnType);
        Local<SuperInvocation> superInvocation = code.newLocal(TypeId.get(SuperInvocation.class));
        Local<Class> thisClass = code.newLocal(TypeId.get(Class.class));

        ConstructorGenerator constructorGenerator = constructorGeneratorFactory
                .newInstance(typeId, original.getSuperclass(), superInvocation, code);
        constructorGenerator.declareLocals();

        code.loadConstant(thisClass, original.getSuperclass());
        int firstReg = (int) Whitebox.getField(locals.returnValue, "reg");
        if (firstReg != 256) {
            throw new RuntimeException("got first reg as: " + firstReg);
        }
        MethodId getEasiestInvocation = TypeId.get(SuperInvocation.class)
                .getMethod(
                        TypeId.get(SuperInvocation.class),
                        "getEasiestInvocation",
                        TypeId.get(Class.class));

        code.invokeStatic(getEasiestInvocation, superInvocation, thisClass);

        generateInvokeWithoutReturn(code, typeId, returnType, parameterTypes, original, modifiers | Modifier.STATIC, EyePatchClassBuilder.PRE_CONSTRUCT, locals);
        constructorGenerator.invokeSuper();

        generateMethodContentsInternal(code, typeId, returnType, parameterTypes, original, modifiers, methodName, locals);
        generateUnsupportedLabel(code, locals);
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

        generateBypassLabel(code, typeId, returnType, locals);
    }

    private void generateUnsupportedLabel(Code code, Locals locals) {
        code.mark(locals.defaultImplementation);
        code.newInstance(
                locals.uoe,
                TypeId.get(UnsupportedOperationException.class).getConstructor());
        code.throwValue(locals.uoe);
    }

    private void generateBypassLabel(Code code, TypeId<?> typeId, TypeId<?> returnType, Locals locals) {
        code.mark(locals.defaultImplementation);
        if (returnType == TypeId.VOID) {
            code.returnVoid();
        } else {
            code.loadConstant(locals.castedReturnValue, null);
            code.returnValue(locals.castedReturnValue);
        }
    }

    private static void generateMethodContentsInternal(Code code, TypeId typeId, TypeId returnType, Class[] parameterTypes, Class original, int modifiers, String methodName, Locals locals) {
        generateInvokeWithoutReturn(code, typeId, returnType, parameterTypes, original, modifiers, methodName, locals);

        if (returnType == TypeId.VOID) {
            code.returnVoid();
        } else {
            code.returnValue(locals.castedReturnValue);
        }
    }

    private static void generateInvokeWithoutReturn(Code code, TypeId typeId, TypeId returnType, Class[] parameterTypes, Class original, int modifiers, String methodName, Locals locals) {
        TypeId<?> staticInvoker = TypeId.get(Dispatcher.class);
        TypeId classType =  TypeId.get(Class.class);
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
            code.move(locals.instanceArg, code.getThis(typeId));
            //locals.instanceArg = code.getThis(typeId);
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

        FieldId<?, ?> unhandledValueField =
                staticInvoker.getField(TypeId.OBJECT, "UNHANDLED");
        code.sget(unhandledValueField, locals.unhandledValue);
        code.compare(
                Comparison.EQ,
                locals.defaultImplementation,
                Checks.notNull(locals.returnValue),
                Checks.notNull(locals.unhandledValue));

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
    }

    private static class Locals {
        Label defaultImplementation;
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
        Local<UnsupportedOperationException> uoe;
        Local<Object> unhandledValue;

        public Locals(Code code, TypeId returnType) {
            defaultImplementation = new Label();

            addUnusedLocals(code);
            returnValue = code.newLocal(TypeId.OBJECT);
            callerClass = code.newLocal(TypeId.get(Class.class));
            instanceArg = code.newLocal(TypeId.OBJECT);
            callerMethod = code.newLocal(TypeId.STRING);
            argTypes = code.newLocal(TypeId.get(Class[].class));
            callerArgs = code.newLocal(TypeId.get(Object[].class));
            castedReturnValue = code.newLocal(returnType);
            parameterLength = code.newLocal(TypeId.INT);
            tmp = code.newLocal(TypeId.OBJECT);
            uoe = code.newLocal(TypeId.get(UnsupportedOperationException.class));
            unhandledValue = code.newLocal(TypeId.OBJECT);

            boxedReturnValue = null;

            if (Primitives.isPrimitive(returnType) && returnType != TypeId.VOID) {
                boxedReturnValue = code.newLocal(Primitives.getBoxedType(returnType));
            }

        }

        /**
         * By adding unused locals, we can ensure that we don't
         * generate any instructions that use the 4 bit addressing
         * scheme to reference registers.
         */
        private void addUnusedLocals(Code code) {
            if (((List)Whitebox.getField(code, "locals")).size() != 0) {
                throw new RuntimeException("locals already defined!");
            }
            for (int i = 0; i < UNUSED_REGISTER_COUNT; i++) {
                code.newLocal(TypeId.OBJECT);
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

}
