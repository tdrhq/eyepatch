// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.renamer;

import java.io.RandomAccessFile;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class RafUtilTest {
    @Rule
    public TemporaryFolder tmpdir = new TemporaryFolder();

    @Test
    public void testInOutInt() throws Throwable {
        int a = 5256;
        RandomAccessFile raf = new RandomAccessFile(tmpdir.newFile("foo"), "rw");
        RafUtil.writeUInt(raf, a);
        raf.seek(0);
        assertEquals(5256, RafUtil.readUInt(raf));
    }

}
