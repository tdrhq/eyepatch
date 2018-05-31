package com.tdrhq.eyepatch.util;

import dalvik.system.PathClassLoader;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ClassLoaderIntrospectorTest {
    @Test
    public void testgetOriginalDexPath() throws Throwable {
        assertThat(
                ClassLoaderIntrospector.getOriginalDexPath(getClass().getClassLoader()),
                hasSize(greaterThan(1)));
    }

    @Test
    public void testAddDexPath() throws Throwable {
        ClassLoader classLoader = new PathClassLoader("", null, getClass().getClassLoader().getParent());
        assertThat(ClassLoaderIntrospector.getOriginalDexPath(classLoader),
                   is(empty()));



    }

    @Test
    public void testClone() throws Throwable {
        ClassLoader clone = ClassLoaderIntrospector.clone(
                getClass().getClassLoader());
        Class clonedClass = clone.loadClass(Foo.class.getName());
        assertThat(
                clonedClass,
                not(sameInstance((Class) Foo.class)));

        assertThat(
                clonedClass.getClassLoader(),
                not(sameInstance(getClass().getClassLoader())));

    }

    public static class Foo {
    }
}
