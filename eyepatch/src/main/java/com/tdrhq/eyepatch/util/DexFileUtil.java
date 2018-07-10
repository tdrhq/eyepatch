// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;

public class DexFileUtil {
    private DexFileUtil() {
    }

    public static DexBackedDexFile readDexFile(File ret) throws IOException {
        InputStream fis = new BufferedInputStream(new FileInputStream(ret));
        InputStream dexFileExtracted = extractDexFile(fis);
        InputStream is = new BufferedInputStream(dexFileExtracted);
        try {
            return DexBackedDexFile.fromInputStream(Opcodes.forApi(16), is);
        } finally {
            is.close();
            fis.close();
        }
    }

    public static ClassDef findClassDef(DexFile dexfile, Class klass) {
        Set<? extends ClassDef> classes = dexfile.getClasses();
        String name = "L" + klass.getName().replace(".", "/") + ";";
        for (ClassDef classDef : classes) {
            if (classDef.getType().equals(name)) {
                return classDef;
            }
        }
        return null;
    }

    private static InputStream extractDexFile(InputStream input) throws IOException {
        input.mark(10);
        if (input.read() != 0x50) {
            input.reset();
            return input;
        }

        input.reset();
        return extractJarFile(input);
    }

    private static InputStream extractJarFile(InputStream input) throws IOException {
        ZipInputStream zis = new ZipInputStream(input);

        ZipEntry entry;

        while ((entry = zis.getNextEntry()) != null) {
            if (entry.getName().equals("classes.dex")) {
                return zis;
            }

            zis.closeEntry();
        }
        throw new RuntimeException("did not find classes.dex");
    }



}
