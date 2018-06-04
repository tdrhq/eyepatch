// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import com.android.dex.DexFormat;
import com.android.dx.DexMaker;
import com.android.dx.TypeId;
import dalvik.system.DexFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class Util {
    private Util() {
    }

    public static DexFile createDexFile(DexMaker dexmaker, File outputFile) throws IOException {
        byte[] dex = dexmaker.generate();

        JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(outputFile));
        JarEntry entry = new JarEntry(DexFormat.DEX_IN_JAR_NAME);
        entry.setSize(dex.length);
        jarOut.putNextEntry(entry);
        jarOut.write(dex);
        jarOut.closeEntry();
        jarOut.close();

        return new DexFile(outputFile);
    }

    public static TypeId<?>  createTypeIdForName(String name) {
        return TypeId.get("L" + name.replace(".", "/") + ";");
    }
}
