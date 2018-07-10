// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import android.support.test.espresso.core.internal.deps.guava.collect.ImmutableSet;
import android.util.Log;
import com.tdrhq.eyepatch.EyePatchTemporaryFolder;
import com.tdrhq.eyepatch.util.ClassLoaderIntrospector;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.immutable.ImmutableDexFile;
import org.jf.dexlib2.rewriter.DexRewriter;
import org.jf.dexlib2.rewriter.RewriterModule;
import org.junit.Rule;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class MergerTest {
    @Rule
    public EyePatchTemporaryFolder tmpdir = new EyePatchTemporaryFolder();

    /**
     * Creates a new DexFile with just the given class extracted out.
     */
    private File extractClass(Class klass)  throws IOException {
        File ret = ClassLoaderIntrospector.getDefiningDexFile(klass);
        File dexFileExtracted = extractDexFile(ret, tmpdir.newFile("anotheroutput.dex"));
        InputStream is = new BufferedInputStream(new FileInputStream(dexFileExtracted));
        DexBackedDexFile dexfile = DexBackedDexFile.fromInputStream(Opcodes.getDefault(), is);
        is.close();
        Log.i("MergerTest", "Finished reading the DexBackedDexFile");

        DexRewriter rewriter = new DexRewriter(new RewriterModule() {
            });
        DexFile rewrittenDexFile = rewriter.rewriteDexFile(dexfile);


        File tmpOutput = tmpdir.newFile("tmpoutput.dex");
        Set<? extends ClassDef> classes = dexfile.getClasses();
        ClassDef theClassDef = null;
        for (ClassDef classDef : classes) {
            if (classDef.getType().equals("L" + klass.getName().replace(".", "/") + ";")) {
                theClassDef = classDef;
            }
        }

        assertNotNull(theClassDef);
        DexFile copy = new ImmutableDexFile(
                Opcodes.getDefault(),
                ImmutableSet.of(theClassDef));
        DexFileFactory.writeDexFile(tmpOutput.toString(), copy);
        return tmpOutput;
    }


    File extractDexFile(File input, File output) throws IOException {
        if (input.toString().endsWith(".dex")) {
            return input;
        }

        extractJarFile(input, output);
        return output;
    }

    private void extractJarFile(File input, File output) throws IOException {
        JarFile jarFile = new JarFile(input);
        JarEntry entry = jarFile.getJarEntry("classes.dex");
        InputStream is = jarFile.getInputStream(entry);

        byte[] data = new byte[1000];
        int length;
        FileOutputStream os = new FileOutputStream(output);
        while ((length = is.read(data)) > 0){
            os.write(data, 0, length);
        }

        os.close();
        is.close();
        jarFile.close();
    }


    @Test
    public void testPreconditions() throws Throwable {
        File file = extractClass(Foo.class);
        assertNotNull(file);
        assertThat(Collections.list(new dalvik.system.DexFile(file).entries()),
                   hasItem(Foo.class.getName()));
    }

    static class Foo {
    }
}
