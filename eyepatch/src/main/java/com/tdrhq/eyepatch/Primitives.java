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
        if (primitive.equals(TypeId.INT)) {
            return TypeId.get(Integer.class);
        } else if (primitive.equals(TypeId.FLOAT)) {
            return TypeId.get(Float.class);
        }
        throw new RuntimeException("not supported");
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
        if (primitive.equals(TypeId.INT)) {
            return "intValue";
        }
        if (primitive.equals(TypeId.FLOAT)) {
            return "floatValue";
        }

        throw new RuntimeException("not supported");
    }

}
