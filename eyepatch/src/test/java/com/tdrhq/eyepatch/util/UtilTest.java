// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class UtilTest {
    @Test
    public void testIsJvm() throws Throwable {
        assertTrue(Util.isJvm());
    }
}
