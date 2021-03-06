package com.tdrhq.eyepatch.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;

public class Whitebox {
    public static class Arg {
        public Class type;
        public Object value;
    }

    public static <T> Arg arg(Class<T> type, T value) {
        Arg arg = new Arg();
        arg.type = type;
        arg.value = value;
        return arg;
    }

    public static Arg arg(Object value) {
        Arg arg = new Arg();
        arg.type = value.getClass();
        arg.value = value;
        return arg;
    }

    private static Class[] getClasses(Arg... args) {
        Class[] ret = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            ret[i] = args[i].type;
        }
        return ret;
    }

    private static Object[] getValues(Arg... args) {
        Object[] ret = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            ret[i] = args[i].value;
        }
        return ret;
    }


    public static Object invoke(Object instance, String name, Arg... args) {
        try {
            Method method = instance.getClass().getDeclaredMethod(name, getClasses(args));
            method.setAccessible(true);
            return method.invoke(instance, getValues(args));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(
                    "No such method, shoud be one of: " +
                    getMethodList(instance.getClass()),
                    e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object invokeStatic(Class klass, String name, Arg... args) {
        try {
            Method method = klass.getDeclaredMethod(name, getClasses(args));
            method.setAccessible(true);
            return method.invoke(null, getValues(args));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(
                    "No such method, shoud be one of: " +
                    getMethodList(klass),
                    e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getField(Object instance, Class type, String fieldName) {
        try {
            Field field = type.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(instance);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(
                    "No such field, should be one of: " +
                    getFieldList(type),
                    e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setField(Object instance, Class type, String fieldName, Object value) {
        try {
            Field field = type.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(
                    "No such field, should be one of: " +
                            getFieldList(type),
                    e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getFieldList(Class type) {
        Field[] fields = type.getDeclaredFields();
        String ret = "";
        for (Field f: fields) {
            ret += f.getName() + "; ";
        }
        return ret;
    }

    private static String getMethodList(Class type) {
        Method[] methods = type.getDeclaredMethods();
        String ret = "";
        for (Method f: methods) {
            ret += f.getName() + "; ";
        }
        return ret;
    }

    public static Object getField(Object instance, String fieldName) {
        return getField(instance, instance.getClass(), fieldName);
    }


    public static Object getStaticField(Class<?> aClass, String fieldName) {
        try {
            Field field = aClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object makeNewInstance(Class dexPathListClass, Class[] classes, Object... initArgs)  {
        Constructor constructor = null;
        try {
            constructor = dexPathListClass
                    .getConstructor(classes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        constructor.setAccessible(true);
        try {
            return constructor
                    .newInstance(initArgs);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setStaticField(Class aClass, String fieldName, Object val) {
           try {
            Field field = aClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, val);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
