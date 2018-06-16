// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.renamer;

import org.junit.Test;
import static org.junit.Assert.*;

public class StaticDexProviderTest {
    @Test
    public void testSimpleCheck() throws Throwable {
        assertEquals(0x3, StaticDexProvider.getBytes()[0x40]);
    }
}
