// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.util;

import com.android.dx.TypeId;
import com.google.common.collect.Lists;
import java.lang.Iterable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Sorter {
    public static List<Method> sortMethods(Iterable<Method> methods) {
        List<Method> copy = Lists.newArrayList(methods);
        Collections.sort(copy, methodComparator);
        return copy;
    }

    static class MethodDeets {
        String name;
        TypeId[] args;

        public String getName() {
            return name;
        }

        public TypeId[] getParameterTypes() {
            return args;
        }
    }

    static Comparator<Method> methodComparator = new Comparator<Method>() {
            @Override
            public int compare(Method one, Method two) {
                MethodDeets oned = getDeets(one);
                MethodDeets twod = getDeets(two);
                return methodDeetsComparator.compare(oned, twod);
            }

            private MethodDeets getDeets(Method method) {
                MethodDeets ret = new MethodDeets();
                ret.name = method.getName();
                Class[] args = method.getParameterTypes();
                ret.args = new TypeId[args.length];
                for (int i = 0; i < args.length; i++) {
                    ret.args[i] = TypeId.get(args[i]);
                }
                return ret;
            }
        };

    private static Comparator<MethodDeets> methodDeetsComparator = new Comparator<MethodDeets>() {
            @Override
            public int compare(MethodDeets one, MethodDeets two) {
                int compare = one.getName().compareTo(two.getName());
                if (compare != 0) {
                    return compare;
                }

                TypeId[] onePs = one.getParameterTypes();
                TypeId[] twoPs = two.getParameterTypes();

                if (onePs.length != twoPs.length) {
                    return onePs.length - twoPs.length;
                }

                for (int i = 0; i < onePs.length; i++) {
                    compare = onePs[i].toString().compareTo(twoPs[i].toString());
                    if (compare != 0) {
                        return compare;
                    }
                }

                return 0;
            }
        };
}
