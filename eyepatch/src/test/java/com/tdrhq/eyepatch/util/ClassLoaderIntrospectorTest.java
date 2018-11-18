// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.util;

import dalvik.system.DexFile;
import java.io.File;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class ClassLoaderIntrospectorTest {
    @Rule
    public TemporaryFolder tmpdir = new TemporaryFolder();

    @Test
    public void testDefiningDexFile() throws Throwable {
        File definingFile = ClassLoaderIntrospector.getDefiningDexFile(tmpdir.getRoot(), Foo.class);
        assertNotNull(definingFile);
        assertTrue(definingFile.exists());
        assertTrue(definingFile.length() > 0);
    }

    public static class Foo {
    }
}
