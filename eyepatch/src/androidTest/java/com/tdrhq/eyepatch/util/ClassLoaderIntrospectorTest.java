package com.tdrhq.eyepatch.util;

import dalvik.system.DexFile;
import java.io.File;
import java.util.Collections;
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

    @Test
    public void testDefiningDexFile() throws Throwable {
        File definingFile = ClassLoaderIntrospector.getDefiningDexFile(Foo.class);
        assertNotNull(definingFile);

        DexFile dexFile = new DexFile(definingFile);

        assertThat(Collections.list(dexFile.entries()), hasItem(Foo.class.getName()));
    }

    public static class Foo {
    }
}
