// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import com.android.dx.DexMaker;
import com.android.dx.TypeId;
import dalvik.system.DexFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Util {
    private Util() {
    }

    public static DexFile createDexFile(DexMaker dexmaker, File outputFile) throws IOException {
        File optFile = new File(outputFile.getAbsolutePath() + ".opt.dex");
        return createDexFile(dexmaker, outputFile, optFile);
    }

    private static DexFile createDexFile(DexMaker dexmaker, File outputFile, File cacheDir) throws IOException {
        writeDexFile(dexmaker, outputFile);

        return DexFile.loadDex(outputFile.getAbsolutePath(),
                               cacheDir != null ? cacheDir.getAbsolutePath() : null,
                               0);
    }

    public static void writeDexFile(DexMaker dexmaker, File outputFile) throws IOException {
        if (!outputFile.getAbsolutePath().endsWith(".dex")) {
            throw new IllegalArgumentException("bad extension");
        }
        byte[] dex = dexmaker.generate();

        FileOutputStream os = new FileOutputStream(outputFile);
        os.write(dex);
        os.close();
    }

    public static TypeId<?>  createTypeIdForName(String name) {
        return TypeId.get("L" + name.replace(".", "/") + ";");
    }
}
