package com.tdrhq.eyepatch;

import com.android.dx.*;
import java.io.*;
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
        generateFooMethod(dexmaker, typeId, original);
        return dexmaker;
    }

    private void generateFooMethod(DexMaker dexmaker, TypeId<?> typeId, Class original) {
        MethodId foo = typeId.getMethod(TypeId.STRING, "foo");
        Code code = dexmaker.declare(foo, Modifier.PUBLIC | Modifier.STATIC);

        TypeId staticInvoker = TypeId.get(StaticInvocationHandler.class);
        TypeId classType = TypeId.get(Class.class);
        TypeId objectType = TypeId.get(Object.class);
        TypeId stringType = TypeId.get(String.class);
        TypeId objectArType = TypeId.get(Object[].class);

        MethodId invokeStaticMethod = staticInvoker.getMethod
            (objectType,
             "invokeStatic",
             classType,
             stringType,
             objectArType);

        Local<Object> returnValue = code.newLocal(TypeId.OBJECT);

        Local<Class> callerClass = code.newLocal(TypeId.get(Class.class));
        Local<String> callerMethod = code.newLocal(TypeId.STRING);
        Local<Object[]> callerArgs = code.newLocal(TypeId.get(Object[].class));
        Local<String> castedReturnValue = code.newLocal(TypeId.STRING);

        code.loadConstant(callerClass, original);
        code.loadConstant(callerMethod, "foo");
        code.loadConstant(callerArgs, null);
        code.invokeStatic(invokeStaticMethod, returnValue, callerClass, callerMethod, callerArgs);

        code.cast(castedReturnValue, returnValue);
        code.returnValue(castedReturnValue);
    }

    public static void invokeHelper(Class klass, String name) {
    }
}
