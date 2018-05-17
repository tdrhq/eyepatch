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

    @Test
    public void testGetBoxedtype() throws Throwable {
        checkBoxType(Integer.class, int.class);
        checkBoxType(Byte.class, byte.class);
        checkBoxType(Character.class, char.class);
        checkBoxType(Double.class, double.class);
        checkBoxType(Float.class, float.class);
        checkBoxType(Long.class, long.class);
        checkBoxType(Short.class, short.class);
    }

    private void checkBoxType(Class box, Class primitive) {
        assertEquals(TypeId.get(box),
                     Primitives.getBoxedType(TypeId.get(primitive)));
    }

    @Test
    public void testCheckUnboxFunction() throws Throwable {
        for (Class primitive : Primitives.allPrimitives) {
            String boxClassName = Primitives.getBoxedType(TypeId.get(primitive)).getName();
            Class boxClass = Class.forName(
                    boxClassName.substring(1, boxClassName.length() -1 ).replace("/", "."));

            boxClass.getMethod(Primitives.getUnboxFunction(TypeId.get(primitive)));
        }
    }
}
