// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.renamer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class AnnotationUtil {
    public static List<Field> getAnnotatedFields(Class klass) {
        Field[] fields = klass.getDeclaredFields();
        Arrays.sort(fields, new Comparator<Field>() {
                @Override
                public int compare(Field field, Field t1) {
                    if (getIndex(field) == getIndex(t1) && getIndex(field) >= 0) {
                        throw new RuntimeException("multiple fields have the same index");
                    }
                    return getIndex(field) - getIndex(t1);
                }
            });
        List<Field> ret = new ArrayList<>();
        for (Field field : fields) {
            if (getIndex(field) >= 0) {
                ret.add(field);
            }
        }
        return ret;
    }

    public static int getIndex(Field field) {
        F f = field.getAnnotation(F.class);
        if (f == null) {
            return -1;
        }
        return f.idx();
    }
}
