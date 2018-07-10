// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;

public class DexFileReader {
    private DexFileReader() {
    }

    public static DexBackedDexFile readDexFile(File ret) throws IOException {
        InputStream dexFileExtracted = extractDexFile(ret);
        InputStream is = new BufferedInputStream(dexFileExtracted);
        try {
            return DexBackedDexFile.fromInputStream(Opcodes.getDefault(), is);
        } finally {
            is.close();
        }
    }

    private static InputStream extractDexFile(File input) throws IOException {
        if (input.toString().endsWith(".dex")) {
            return new FileInputStream(input);
        }

        return extractJarFile(input);
    }

    private static InputStream extractJarFile(File input) throws IOException {
        final JarFile jarFile = new JarFile(input);
        JarEntry entry = jarFile.getJarEntry("classes.dex");
        InputStream is = jarFile.getInputStream(entry);

        return new FilterInputStream(is) {
            @Override
            public void close() throws IOException {
                super.close();
                jarFile.close();
            }
        };
    }



}
