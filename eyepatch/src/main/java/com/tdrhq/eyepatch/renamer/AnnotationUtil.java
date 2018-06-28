// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.renamer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotationUtil {
    static Map<Class, List<Field>> annotationFieldsCache = new HashMap<>();

    public static List<Field> getAnnotatedFields(Class klass) {
        if (annotationFieldsCache.containsKey(klass)) {
            return annotationFieldsCache.get(klass);
        }

        List<Field> ret = getAnnotatedFieldsUncached(klass);
        annotationFieldsCache.put(klass, ret);
        return ret;
    }

    private static List<Field> getAnnotatedFieldsUncached(Class klass) {
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

        if (ret.size() == 0) {
            throw new RuntimeException("no annotated fields found for " + klass);
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

    public static int getSizeFromSizeIdx(Object instance, Field f) throws IllegalAccessException {
        List<Field> fields = getAnnotatedFields(instance.getClass());
        int size = -1;
        int sizeIdx = f.getAnnotation(F.class).sizeIdx();
        for (Field sizeField : fields) {
            if (AnnotationUtil.getIndex(sizeField) == sizeIdx) {
                size = (int) sizeField.get(instance);
            }
        }
        if (size == -1) {
            throw new RuntimeException("could not find index: " + sizeIdx);
        }
        return size;
    }
}
