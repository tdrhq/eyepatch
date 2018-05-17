package com.tdrhq.eyepatch;

import com.android.dx.TypeId;
import org.junit.Test;
import static org.junit.Assert.*;

public class PrimitivesTest {

    @Test
    public void testGetBoxedType() throws Throwable {
        assertEquals(TypeId.get(Integer.class), Primitives.getBoxedType(TypeId.INT));
        assertEquals(TypeId.get(Float.class), Primitives.getBoxedType(TypeId.FLOAT));
    }

    @Test
    public void testForAllPrimitives() throws Throwable {
        for (Class primitive : Primitives.allPrimitives) {
            assertTrue(Primitives.isPrimitive(TypeId.get(primitive)));
        }
        assertFalse(Primitives.isPrimitive(TypeId.STRING));
    }
}
