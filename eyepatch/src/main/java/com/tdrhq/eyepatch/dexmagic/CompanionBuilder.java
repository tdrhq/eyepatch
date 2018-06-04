// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import android.util.Log;
import com.android.dx.Code;
import com.android.dx.DexMaker;
import com.android.dx.Local;
import com.android.dx.MethodId;
import com.android.dx.TypeId;
import dalvik.system.DexFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ClassLoader;
import java.lang.UnsupportedOperationException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class CompanionBuilder {
    private File mDataDir;
    private static int counter = 0;

    public CompanionBuilder(File dataDir) {
        mDataDir = dataDir;
    }

    /**
     * Generate a companion class for the given class.
     *
     * The ClassLoader needs to be the *real* classloader. Note that
     * we don't need to cache the result, because we use a static
     * counter to generate a new name everytime.
     */
    public Class build(Class realClass, ClassLoader classLoader) {
        String name = generateName();
        DexMaker dexmaker = buildDexMaker(name, realClass);
        try {
            byte[] dex = dexmaker.generate();

            File of = new File(mDataDir, name +  ".dex");
            FileOutputStream os = new FileOutputStream(of);
            os.write(dex);
            os.close();

            DexFile dexFile = DexFile.loadDex(of.getAbsolutePath(), null, 0);
            return dexFile.loadClass(name, classLoader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private DexMaker buildDexMaker(String name, Class original) {
        DexMaker dexmaker = new DexMaker();
        TypeId<?> typeId = TypeId.get("L" + name.replace(".", "/") + ";");
        dexmaker.declare(typeId, name + ".generated", Modifier.PUBLIC | Modifier.ABSTRACT, TypeId.OBJECT);

        for (Method methodTemplate : original.getDeclaredMethods()) {
            if (Modifier.isStatic(methodTemplate.getModifiers())) {
                Log.i("CompanionBuilder", "static method: " + methodTemplate.getName());
                generateMethod(dexmaker, methodTemplate, typeId, original);
            }
        }

        return dexmaker;

    }

    public void generateMethod(DexMaker dexmaker, Method methodTemplate, TypeId<?> typeId, Class original) {
        String methodName = methodTemplate.getName();
        int modifiers = methodTemplate.getModifiers();
        modifiers &= (~(Modifier.STATIC | Modifier.FINAL| Modifier.NATIVE));
        modifiers |= Modifier.PUBLIC;
        TypeId returnType = TypeId.get(methodTemplate.getReturnType());
        Class[] parameterTypes = methodTemplate.getParameterTypes();

        TypeId[] arguments = new TypeId[parameterTypes.length];
        for (int i = 0 ;i < parameterTypes.length; i++) {
            arguments[i] = TypeId.get(parameterTypes[i]);
        }
        MethodId foo = typeId.getMethod(returnType, methodName, arguments);
        Code code = dexmaker.declare(foo, modifiers);
        TypeId<UnsupportedOperationException> exType = TypeId.get(UnsupportedOperationException.class);
        Local<UnsupportedOperationException> local = code.newLocal(exType);

        code.newInstance(local, exType.getConstructor());
        code.throwValue(local);
    }

    private String generateName() {
        synchronized (CompanionBuilder.class) {
            return "com.tdrhq.eyepatch.dexmagic.Companion" + (++counter);
        }
    }
}
