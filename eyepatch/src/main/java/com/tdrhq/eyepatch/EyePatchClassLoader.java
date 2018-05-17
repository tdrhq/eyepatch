package com.tdrhq.eyepatch;

import com.android.dx.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class EyePatchClassLoader {
    private File mDataDir;
    public EyePatchClassLoader(File dataDir) {
        mDataDir = dataDir;
    }

    public Class wrapClass(Class realClass) {
        DexMaker dexmaker = buildDexMaker(realClass.getName(), realClass);
        try {
            ClassLoader loader = dexmaker.generateAndLoad(
                    new ClassLoaderWithBlacklist(realClass.getClassLoader(), realClass),
                    mDataDir);
            Class ret =  loader.loadClass(realClass.getName());
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private DexMaker buildDexMaker(String name, Class original) {
        DexMaker dexmaker = new DexMaker();
        TypeId<?> typeId = TypeId.get("L" + name.replace(".", "/") + ";");
        dexmaker.declare(typeId, name + ".generated", Modifier.PUBLIC, TypeId.OBJECT);

        for (Constructor constructor : original.getDeclaredConstructors()) {
            generateConstructor(dexmaker, constructor, typeId, original);
        }

        for (Method methodTemplate : original.getDeclaredMethods()) {
            generateMethod(dexmaker, methodTemplate, typeId, original);
        }
        return dexmaker;
    }

    private void generateConstructor(DexMaker dexmaker, Constructor constructor, final TypeId<?> typeId, Class original) {
        String methodName = "__construct__";
        int modifiers = constructor.getModifiers();
        TypeId returnType = TypeId.VOID;
        Class[] parameterTypes = constructor.getParameterTypes();
        TypeId[] arguments = new TypeId[parameterTypes.length];
        for (int i = 0 ;i < parameterTypes.length; i++) {
            arguments[i] = TypeId.get(parameterTypes[i]);
        }
        MethodId cons = typeId.getConstructor();
        final TypeId parent = TypeId.get(original.getSuperclass());
        final Code  code = dexmaker.declare(cons, Modifier.PUBLIC);
        generateMethodContents(
                code,
                typeId,
                returnType,
                parameterTypes,
                original,
                modifiers,
                methodName,
                new Runnable() {
                    @Override
                    public void run() {
                        code.invokeDirect(parent.getConstructor(), null, code.getThis(typeId));

                    }
                });
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
        generateMethodContents(code, typeId, returnType, parameterTypes, original, modifiers,
                               methodName, new Runnable() {
                                       @Override
                                       public void run() {
                                       }
                                   });
    }

    private void generateMethodContents(
            Code code,
            TypeId typeId,
            TypeId returnType, Class[] parameterTypes, Class original,
            int modifiers,
            String methodName,
            Runnable afterLocals) {
        TypeId staticInvoker = TypeId.get(StaticInvocationHandler.class);
        TypeId classType = TypeId.get(Class.class);
        TypeId instance = TypeId.OBJECT;
        TypeId objectType = TypeId.get(Object.class);
        TypeId stringType = TypeId.get(String.class);
        TypeId objectArType = TypeId.get(Object[].class);

        MethodId invokeStaticMethod = staticInvoker.getMethod(
                objectType,
                "invokeStatic",
                classType,
                objectType,
                stringType,
                objectArType);

        Local<Object> returnValue = null;

        if (returnType != TypeId.VOID) {
            returnValue = code.newLocal(TypeId.OBJECT);
        }

        Local<Class> callerClass = code.newLocal(TypeId.get(Class.class));
        Local instanceArg = code.newLocal(TypeId.OBJECT);
        Local<String> callerMethod = code.newLocal(TypeId.STRING);
        Local<Object[]> callerArgs = code.newLocal(TypeId.get(Object[].class));
        Local castedReturnValue = code.newLocal(returnType);
        Local<Integer> parameterLength = code.newLocal(TypeId.INT);
        Local<Object> tmp = code.newLocal(TypeId.OBJECT);

        Local boxedReturnValue = null;

        if (Primitives.isPrimitive(returnType) && returnType != TypeId.VOID) {
            boxedReturnValue = code.newLocal(Primitives.getBoxedType(returnType));
        }

        afterLocals.run();
        code.loadConstant(parameterLength, parameterTypes.length);

        buildCallerArray(callerArgs, parameterLength, tmp, parameterTypes, code);

        code.loadConstant(callerClass, original);
        if (Modifier.isStatic(modifiers)) {
            code.loadConstant(instanceArg, null);
        } else {
            instanceArg = code.getThis(typeId);
        }
        code.loadConstant(callerMethod, methodName);
        code.invokeStatic(
                invokeStaticMethod,
                returnValue,
                callerClass,
                instanceArg,
                callerMethod,
                callerArgs);

        if (Primitives.isPrimitive(returnType)) {
            MethodId intValue = Primitives.getBoxedType(returnType)
                    .getMethod(
                            returnType,
                            Primitives.getUnboxFunction(returnType));
            code.cast(boxedReturnValue, returnValue);
            code.invokeVirtual(
                    intValue,
                    castedReturnValue,
                    boxedReturnValue);

        } else if (returnType != TypeId.VOID) {
            code.cast(castedReturnValue, returnValue);
        }

        if (returnType == TypeId.VOID) {
            code.returnVoid();
        } else {
            code.returnValue(castedReturnValue);
        }
    }

    private void buildCallerArray(
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

    public static void invokeHelper(Class klass, String name) {
    }
}
