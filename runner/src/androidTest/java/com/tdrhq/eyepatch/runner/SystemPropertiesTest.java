package com.tdrhq.eyepatch.runner;

import com.tdrhq.eyepatch.util.SystemProperties;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class SystemPropertiesTest {
    @Test
    public void testSimpleCheck() throws Throwable {
        assertThat(SystemProperties.getSystemProperty("ro.product.device"),
                   not(isEmptyString()));
        assertThat(SystemProperties.getSystemProperty("does.not.exist"),
                   is(equalTo("")));
    }
}
