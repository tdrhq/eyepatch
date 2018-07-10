// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import android.support.test.espresso.core.internal.deps.guava.collect.ImmutableSet;
import android.util.Log;
import com.tdrhq.eyepatch.EyePatchTemporaryFolder;
import com.tdrhq.eyepatch.util.ClassLoaderIntrospector;
import com.tdrhq.eyepatch.util.DexFileUtil;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.immutable.ImmutableDexFile;
import org.jf.dexlib2.writer.io.FileDataStore;
import org.jf.dexlib2.writer.pool.DexPool;
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
        DexBackedDexFile dexfile = DexFileUtil.readDexFile(ret);
        Log.i("MergerTest", "Finished reading the DexBackedDexFile");

        File tmpOutput = tmpdir.newFile("tmpoutput.dex");
        ClassDef theClassDef = DexFileUtil.findClassDef(dexfile, klass);

        assertNotNull(theClassDef);
        DexFile copy = new ImmutableDexFile(
                Opcodes.forApi(16),
                ImmutableSet.of(theClassDef));
        FileDataStore dataStore = new FileDataStore(tmpOutput);
        DexPool.writeTo(dataStore, copy);
        dataStore.close();
        return tmpOutput;
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
