// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import com.android.dx.DexMaker;
import dalvik.system.DexFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Util {
    private Util() {
    }

    public static DexFile createDexFile(DexMaker dexmaker, File outputFile) throws IOException {
        byte[] dex = dexmaker.generate();

        FileOutputStream os = new FileOutputStream(outputFile);
        os.write(dex);
        os.close();

        return new DexFile(outputFile);
    }

}
