package com.tdrhq.eyepatch.classloader;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class AndroidClassLoaderTest {
    private AndroidClassLoader classLoader;

    @Before
    public void before() throws Throwable {
        classLoader = new AndroidClassLoader(getClass().getClassLoader());
    }

    @Test
    public void testSimpleCreation() throws Throwable {
    }
}
