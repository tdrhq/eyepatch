// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import com.tdrhq.eyepatch.util.Checks;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.WeakHashMap;

public class ShadowClassHandler implements ClassHandler {
    private Class originalClass;
    private Class shadowClass;
    private WeakHashMap objectToShadowMap = new WeakHashMap();

    public ShadowClassHandler(Class originalClass, Class shadowClass) {
        this.originalClass = Checks.notNull(originalClass);
        this.shadowClass = Checks.notNull(shadowClass);
    }

    @Override
    public Object handleInvocation(Invocation invocation) {

        if (invocation.getMethod().equals("__construct__")) {
            try {
                Object shadow = shadowClass.newInstance();
                objectToShadowMap.put(invocation.getInstance(), shadow);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            Object shadow = objectToShadowMap.get(invocation.getInstance());
            Method method = shadowClass.getMethod(
                    invocation.getMethod(),
                    invocation.getArgTypes());
            method.setAccessible(true);
            return method.invoke(
                    shadow,
                    invocation.getArgs());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class getResponsibility() {
        return originalClass;
    }

}
