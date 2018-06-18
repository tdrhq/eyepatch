// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.renamer;

import com.tdrhq.eyepatch.EyePatchTemporaryFolder;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

public class StaticDexProviderTest {

    @Rule
    public EyePatchTemporaryFolder tmpdir = new EyePatchTemporaryFolder();

    @Test
    public void testSimpleCheck() throws Throwable {
        assertEquals(0x3, StaticDexProvider.getBytes()[0x40]);
    }

    @Test
    public void testWriteFile() throws Throwable {
        StaticDexProvider.writeToFile(tmpdir.newFile("foo.dex"));
    }
}
