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
        File optFile = new File(outputFile.getAbsolutePath() + ".opt.jar");
        return createDexFile(dexmaker, outputFile, optFile);
    }

    private static DexFile createDexFile(DexMaker dexmaker, File outputFile, File cacheDir) throws IOException {
        byte[] dex = dexmaker.generate();

        FileOutputStream os = new FileOutputStream(outputFile);
        os.write(dex);
        os.close();

        return DexFile.loadDex(outputFile.getAbsolutePath(),
                               cacheDir != null ? cacheDir.getAbsolutePath() : null,
                               0);
    }

    public static TypeId<?>  createTypeIdForName(String name) {
        return TypeId.get("L" + name.replace(".", "/") + ";");
    }
}
