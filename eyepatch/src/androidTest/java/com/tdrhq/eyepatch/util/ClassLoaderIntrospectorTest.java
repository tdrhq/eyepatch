package com.tdrhq.eyepatch.util;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class ClassLoaderIntrospectorTest {
    @Test
    public void testgetOriginalDexPath() throws Throwable {
        assertThat(
                ClassLoaderIntrospector.getOriginalDexPath(getClass().getClassLoader()),
                hasSize(greaterThan(1)));
    }
}
