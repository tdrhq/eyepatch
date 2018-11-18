// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.classloader;

import org.junit.Test;

public class EyePatchClassLoaderJvmTests {
    @Test
    public void testPreconditions() throws Throwable {
        new EyePatchClassLoader(getClass().getClassLoader());
    }
}
