// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch;

import com.android.dx.TypeId;

public class Primitives {
    static final Class[] allPrimitives = new Class[] {
          boolean.class,
          byte.class,
          char.class,
          double.class,
          float.class,
          int.class,
          long.class,
          short.class
    };

    public static TypeId getBoxedType(TypeId primitive) {
        if (primitive == TypeId.BOOLEAN)
            return TypeId.get(Boolean.class);
        if (primitive == TypeId.BYTE)
            return TypeId.get(Byte.class);
        if (primitive == TypeId.CHAR)
            return TypeId.get(Character.class);
        if (primitive ==  TypeId.DOUBLE)
            return TypeId.get(Double.class);
        if (primitive == TypeId.FLOAT)
            return TypeId.get(Float.class);
        if (primitive ==  TypeId.INT)
            return TypeId.get(Integer.class);
        if (primitive == TypeId.LONG)
            return TypeId.get(Long.class);
        if (primitive ==  TypeId.SHORT)
            return TypeId.get(Short.class);

        throw new RuntimeException("unknown type");
    }

    public static boolean isPrimitive(TypeId type) {
        for (Class primitive : allPrimitives) {
            if (type == TypeId.get(primitive)) {
                return true;
            }
        }
        return false;
    }

    public static String getUnboxFunction(TypeId primitive) {
        if (primitive == TypeId.BOOLEAN)
            return "booleanValue";
        if (primitive == TypeId.BYTE)
            return "byteValue";
        if (primitive == TypeId.CHAR)
            return "charValue";
        if (primitive ==  TypeId.DOUBLE)
            return "doubleValue";
        if (primitive == TypeId.FLOAT)
            return "floatValue";
        if (primitive ==  TypeId.INT)
            return "intValue";
        if (primitive == TypeId.LONG)
            return "longValue";
        if (primitive ==  TypeId.SHORT)
            return "shortValue";


        throw new RuntimeException("not supported");
    }

}
