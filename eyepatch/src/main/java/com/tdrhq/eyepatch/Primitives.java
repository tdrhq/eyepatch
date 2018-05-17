// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch;

import com.android.dx.TypeId;

public class Primitives {
    public static TypeId getBoxedType(TypeId primitive) {
        if (primitive.equals(TypeId.INT)) {
            return TypeId.get(Integer.class);
        } else if (primitive.equals(TypeId.FLOAT)) {
            return TypeId.get(Float.class);
        }
        throw new RuntimeException("not supported");
    }

    public static boolean isPrimitive(TypeId type) {
        if (type.equals(TypeId.INT) || type.equals(TypeId.FLOAT)) {
            return true;
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
