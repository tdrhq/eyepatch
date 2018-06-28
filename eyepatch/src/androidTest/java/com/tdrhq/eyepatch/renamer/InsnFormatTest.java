// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.renamer;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class InsnFormatTest {

    @Test
    public void testGetLength() throws Throwable {
        assertEquals(1, InsnFormat.getLength((short) 0));
        assertEquals(2, InsnFormat.getLength(0x2d));
        assertEquals(2, InsnFormat.getLength(0x2e));
        assertEquals(2, InsnFormat.getLength(0x31));
    }

    @Test
    public void testVerifyAllInstructionsAvailable() throws Throwable {
        for (int i = 0; i < 0xff; i++) {
            assertThat(InsnFormat.getLength(i), greaterThan(0));
        }
    }
}
