package com.tdrhq.eyepatch;

import com.android.dx.*;
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
        MethodId foo = typeId.getMethod(returnType, methodTemplate.getName());
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

        Local<Object> returnValue = code.newLocal(TypeId.OBJECT);


        Local<Class> callerClass = code.newLocal(TypeId.get(Class.class));
        Local instanceArg = code.newLocal(TypeId.OBJECT);
        Local<String> callerMethod = code.newLocal(TypeId.STRING);
        Local<Object[]> callerArgs = code.newLocal(TypeId.get(Object[].class));
        Local castedReturnValue = code.newLocal(returnType);

        Local boxedReturnValue = null;

        if (Primitives.isPrimitive(returnType)) {
            boxedReturnValue = code.newLocal(Primitives.getBoxedType(returnType));
        }

        code.loadConstant(callerClass, original);
        if (Modifier.isStatic(methodTemplate.getModifiers())) {
            code.loadConstant(instanceArg, null);
        } else {
            instanceArg = code.getThis(typeId);
        }
        code.loadConstant(callerMethod, methodTemplate.getName());
        code.loadConstant(callerArgs, null);
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

        } else {
            code.cast(castedReturnValue, returnValue);
        }
        code.returnValue(castedReturnValue);
    }

    public static void invokeHelper(Class klass, String name) {
    }
}
