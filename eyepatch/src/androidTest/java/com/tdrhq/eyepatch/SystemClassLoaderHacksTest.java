package com.tdrhq.eyepatch;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dalvik.system.PathClassLoader;

import static org.junit.Assert.*;

public class SystemClassLoaderHacksTest {
    private ClassLoader oldClassLoader;
    private Context context;

    @Before
    public void before() {
        context = InstrumentationRegistry.getTargetContext();
        oldClassLoader = context.getClassLoader();
    }

    @After
    public void after() {
    }

    @Test
    public void testPreconditions() {
        // in theory this should do nothing, but at least it verifies all
        // our reflection is in order.
        ClassLoaderHacks.validateAppClassLoader(context, context.getClassLoader());
    }

    @Test
    public void testTypeOfSystemCL() {
        assertEquals(PathClassLoader.class, ClassLoader.getSystemClassLoader().getClass());
    }

    @Test
    public void validate() throws ClassNotFoundException {
        assertNotNull(InstrumentationRegistry.getTargetContext().getClassLoader().loadClass("org.junit.Test"));
    }

    @Test
    public void systemClassLoaderCantLoadClass() {
        try {
            ClassLoader.getSystemClassLoader().loadClass("org.junit.Test");
            fail("expected exception");
        } catch (ClassNotFoundException e) {
            // expected
        }
    }
}
