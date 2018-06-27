// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.renamer;

import java.io.EOFException;
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

    @Test
    public void testShortInOutInt() throws Throwable {
        short a = 5256;
        RandomAccessFile raf = new RandomAccessFile(tmpdir.newFile("foo"), "rw");
        RafUtil.writeUShort(raf, a);
        raf.seek(0);
        assertEquals(5256, RafUtil.readUShort(raf));
    }


    @Test
    public void testInOutIntULeb() throws Throwable {
        int a = 5256;
        RandomAccessFile raf = new RandomAccessFile(tmpdir.newFile("foo"), "rw");
        RafUtil.writeULeb128(raf, a);
        raf.seek(0);
        assertEquals(5256, RafUtil.readULeb128(raf));
    }

    @Test
    public void testInOutIntULebZero() throws Throwable {
        int a = 0;
        RandomAccessFile raf = new RandomAccessFile(tmpdir.newFile("foo"), "rw");
        RafUtil.writeULeb128(raf, a);
        raf.seek(0);
        assertEquals((byte) 0, raf.readByte());
        try {
            raf.readByte();
            fail("expeced exception");
        } catch (EOFException e) {
            // expected
        }
        raf.seek(0);

        assertEquals(0, RafUtil.readULeb128(raf));
    }

    @Test
    public void testInOutIntULebSigned() throws Throwable {
        int a = -525600;
        RandomAccessFile raf = new RandomAccessFile(tmpdir.newFile("foo"), "rw");
        RafUtil.writeSLeb128(raf, a);
        raf.seek(0);
        assertEquals(-525600, RafUtil.readSLeb128(raf));
    }
}
