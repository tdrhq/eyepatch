package com.tdrhq.eyepatch;

import com.android.dx.TypeId;
import org.junit.Test;
import static org.junit.Assert.*;

public class PrimitivesTest {
    private Class[] allPrimitives = new Class[] {
          boolean.class,
          byte.class,
          char.class,
          double.class,
          float.class,
          int.class,
          long.class,
          short.class
    };

    @Test
    public void testGetBoxedType() throws Throwable {
        assertEquals(TypeId.get(Integer.class), Primitives.getBoxedType(TypeId.INT));
        assertEquals(TypeId.get(Float.class), Primitives.getBoxedType(TypeId.FLOAT));
    }
}
