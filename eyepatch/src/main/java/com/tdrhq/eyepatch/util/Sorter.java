// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.util;

import com.android.dx.TypeId;
import com.google.common.collect.Lists;
import java.lang.Iterable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.jf.dexlib2.iface.MethodParameter;

public class Sorter {
    public static List<Method> sortMethods(Iterable<Method> methods) {
        List<Method> copy = Lists.newArrayList(methods);
        Collections.sort(copy, methodComparator);
        return copy;
    }

    public static List<Method> sortMethods(Method[] methods) {
        List<Method> copy = Lists.newArrayList(methods);
        Collections.sort(copy, methodComparator);
        return copy;
    }

    public static List<org.jf.dexlib2.iface.Method> sortDexlibMethods(Iterable<org.jf.dexlib2.iface.Method> methods) {
        List<org.jf.dexlib2.iface.Method> copy = Lists.newArrayList(methods);
        Collections.sort(copy, dexlibMethodComparator);
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


    static Comparator<org.jf.dexlib2.iface.Method> dexlibMethodComparator = new Comparator<org.jf.dexlib2.iface.Method>() {
            @Override
            public int compare(org.jf.dexlib2.iface.Method one, org.jf.dexlib2.iface.Method two) {
                MethodDeets oned = getDeets(one);
                MethodDeets twod = getDeets(two);
                return methodDeetsComparator.compare(oned, twod);
            }

            private MethodDeets getDeets(org.jf.dexlib2.iface.Method method) {
                MethodDeets ret = new MethodDeets();
                ret.name = method.getName();
                List<? extends MethodParameter> args = method.getParameters();
                ret.args = new TypeId[args.size()];
                for (int i = 0; i < args.size(); i++) {
                    ret.args[i] = TypeId.get(args.get(i).getType());
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
