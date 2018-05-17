package com.tdrhq.eyepatch;

import com.android.dx.*;
import com.android.dx.UnaryOp;
import java.io.*;
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

        generateZeroArgumentConstructor(dexmaker, typeId, original);

        for (Method methodTemplate : original.getDeclaredMethods()) {
            generateMethod(dexmaker, methodTemplate, typeId, original);
        }
        return dexmaker;
    }

    private void generateZeroArgumentConstructor(DexMaker dexmaker, TypeId<?> typeId, Class original) {
        MethodId cons = typeId.getConstructor();
        TypeId parent = TypeId.get(original.getSuperclass());
        Code  code = dexmaker.declare(cons, Modifier.PUBLIC);
        code.invokeDirect(parent.getConstructor(), null, code.getThis(typeId));
        code.returnVoid();
    }

    private void generateMethod(DexMaker dexmaker, Method methodTemplate, TypeId<?> typeId, Class original) {
        TypeId returnType = TypeId.get(methodTemplate.getReturnType());
        Class[] parameterTypes = methodTemplate.getParameterTypes();
        TypeId[] arguments = new TypeId[parameterTypes.length];
        for (int i = 0 ;i < parameterTypes.length; i++) {
            arguments[i] = TypeId.get(methodTemplate.getParameterTypes()[i]);
        }
        MethodId foo = typeId.getMethod(returnType, methodTemplate.getName(), arguments);
        Code code = dexmaker.declare(foo, methodTemplate.getModifiers());

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

        code.loadConstant(parameterLength, parameterTypes.length);

        buildCallerArray(callerArgs, parameterLength, tmp, parameterTypes, code);

        code.loadConstant(callerClass, original);
        if (Modifier.isStatic(methodTemplate.getModifiers())) {
            code.loadConstant(instanceArg, null);
        } else {
            instanceArg = code.getThis(typeId);
        }
        code.loadConstant(callerMethod, methodTemplate.getName());
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
