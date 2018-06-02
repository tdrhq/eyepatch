// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import com.android.dx.DexMaker;
import com.android.dx.TypeId;
import dalvik.system.DexFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ClassLoader;
import java.lang.reflect.Modifier;

public class CompanionBuilder {
    private File mDataDir;
    private static int counter = 0;

    public CompanionBuilder(File dataDir) {
        mDataDir = dataDir;
    }

    public Class build(Class realClass, ClassLoader classLoader) {
        String name = generateName();
        DexMaker dexmaker = buildDexMaker(name, realClass);
        try {
            byte[] dex = dexmaker.generate();

            File of = new File(mDataDir, name +  ".dex");
            FileOutputStream os = new FileOutputStream(of);
            os.write(dex);
            os.close();

            DexFile dexFile = new DexFile(of);
            return dexFile.loadClass(name, classLoader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private DexMaker buildDexMaker(String name, Class original) {
        DexMaker dexmaker = new DexMaker();
        TypeId<?> typeId = TypeId.get("L" + name.replace(".", "/") + ";");
        dexmaker.declare(typeId, name + ".generated", Modifier.PUBLIC, TypeId.OBJECT);
        return dexmaker;

    }


    private String generateName() {
        return "com.tdrhq.eyepatch.dexmagic.Companion" + (++counter);
    }
}
