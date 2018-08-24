package com.tdrhq.eyepatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
    }
}
