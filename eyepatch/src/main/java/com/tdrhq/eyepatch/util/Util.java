// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.util;

import com.android.dx.DexMaker;
import com.android.dx.TypeId;
import dalvik.system.DexFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Util {
    private Util() {
    }

    public static DexFile createDexFile(DexMaker dexmaker, File outputFile) throws IOException {
        writeDexFile(dexmaker, outputFile);
        return loadDexFile(outputFile);
    }

    public static DexFile loadDexFile(File outputFile) throws IOException {
        File cacheDir = new File(outputFile.getAbsolutePath() + ".opt.dex");
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

    public static byte[] getClassBytes(ClassLoader classLoader, String klass) throws ClassNotFoundException {
        String resource = klass.replace(".", "/") + ".class";
        System.out.println("finding "+ resource);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            InputStream stream = classLoader.getResourceAsStream(resource);
            byte[] buff = new byte[2048];

            int len;
            while ((len = stream.read(buff)) > 0) {
                os.write(buff, 0, len);
            }
        } catch (IOException e) {
            throw new ClassNotFoundException("could not find class", e);
        }

        return os.toByteArray();
    }

    public static boolean isJvm() {
        String runtime = System.getProperty("java.vm.name");
        return !runtime.equals("Dalvik");
    }
}
