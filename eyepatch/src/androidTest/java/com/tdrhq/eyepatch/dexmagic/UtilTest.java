// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import com.android.dx.DexMaker;
import com.tdrhq.eyepatch.EyePatchTemporaryFolder;
import com.tdrhq.eyepatch.util.Util;

import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

public class UtilTest {

    @Rule
    public EyePatchTemporaryFolder tmpdir = new EyePatchTemporaryFolder();

    @Test
    public void testThrowsIllegalArgumentException() throws Throwable {
        try {
            Util.createDexFile(new DexMaker(), tmpdir.newFile("foo.jar"));
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testThrowsIllegalArgumentExceptionForWriteFileToo() throws Throwable {
        try {
            Util.writeDexFile(new DexMaker(), tmpdir.newFile("foo.jar"));
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

}
