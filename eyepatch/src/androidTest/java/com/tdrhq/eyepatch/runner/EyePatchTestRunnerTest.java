package com.tdrhq.eyepatch.runner;

import com.tdrhq.eyepatch.classloader.AndroidClassLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(EyePatchTestRunner.class)
public class EyePatchTestRunnerTest {
    @Test
    public void testPreconditions() throws Throwable {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testVerifyClassLoader() throws Throwable {
        assertEquals(
                AndroidClassLoader.class.getName(),
                getClass().getClassLoader().getClass().getName());
    }
}
