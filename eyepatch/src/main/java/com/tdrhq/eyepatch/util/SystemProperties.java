// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.util;

import android.content.Context;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SystemProperties {
    public static String getSystemProperty(String key) {
        try {
            ClassLoader cl = Context.class.getClassLoader();
            Class<?> SystemProperties = cl.loadClass("android.os.SystemProperties");

            Class[] paramTypes = { String.class };
            Method get = SystemProperties.getMethod("get", paramTypes);

            //Parameters
            Object[] params = { key };
            return (String) get.invoke(null, params);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
