package com.tdrhq.eyepatch.dexmagic;

import com.android.dx.DexMaker;
import com.android.dx.TypeId;
import com.tdrhq.eyepatch.util.EyePatchTemporaryFolder;
import com.tdrhq.eyepatch.util.Util;

import dalvik.system.DexFile;
import java.io.File;
import java.lang.reflect.Modifier;
import org.junit.Rule;
import org.junit.Test;

/**
 * Older API levels have some quirks with loading classes, this test
 * tries to irons out those quirks.
 */
public class SimpleDexloadingTest {
    static int counter = 0; // almost never used, but just in case

    @Rule
    public EyePatchTemporaryFolder tmpdir = new EyePatchTemporaryFolder();

    @Test
    public void testHappyPath() throws Throwable {
        DexMaker dexmaker = new DexMaker();
        String className = "com.foo.Bar" + (++counter);
        TypeId<?> typeId = Util.createTypeIdForName(className);
        dexmaker.declare(typeId, className + ".generated", Modifier.PUBLIC, TypeId.OBJECT);

        File of = new File(tmpdir.getRoot(), "blah" + (++counter) + ".dex");
        DexFile dexFile = Util.createDexFile(dexmaker, of);
        dexFile.loadClass(className, getClass().getClassLoader());
    }
}
