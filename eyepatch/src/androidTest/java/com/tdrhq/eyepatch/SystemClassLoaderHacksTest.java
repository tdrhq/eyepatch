package com.tdrhq.eyepatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dalvik.system.PathClassLoader;

import static org.junit.Assert.*;

public class SystemClassLoaderHacksTest {
    private ClassLoader oldClassLoader;

    @Before
    public void before() {
        oldClassLoader = ClassLoader.getSystemClassLoader();
    }

    @After
    public void after() {
        SystemClassLoaderHacks.registerSystemClassLoader(oldClassLoader);
    }

    @Test
    public void testPreconditions() {
        // in theory this should do nothing, but at least it verifies all
        // our reflection is in order.
        SystemClassLoaderHacks.registerSystemClassLoader(oldClassLoader);
    }

    @Test
    public void testTypeOfSystemCL() {
        assertEquals(PathClassLoader.class, ClassLoader.getSystemClassLoader().getClass());
    }
}
