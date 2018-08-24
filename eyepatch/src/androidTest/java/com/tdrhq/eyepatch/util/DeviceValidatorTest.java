package com.tdrhq.eyepatch.util;

import com.tdrhq.eyepatch.util.DeviceValidator;

import org.junit.Test;
import static org.junit.Assert.*;

public class DeviceValidatorTest {
    @Test
    public void testNullReturnsEmpty() throws Throwable {
        assertEquals("", DeviceValidator.parseDexOptFlags(null, "v"));
    }

    @Test
    public void testFlagDoesntExist() throws Throwable {
        assertEquals("", DeviceValidator.parseDexOptFlags("d=k", "v"));
        assertEquals("", DeviceValidator.parseDexOptFlags("v", "v"));
    }

    @Test
    public void testParsesSimpleFlag() throws Throwable {
        assertEquals("n", DeviceValidator.parseDexOptFlags("v=n", "v"));
    }

    @Test
    public void testComplexScenarios() throws Throwable {
        assertEquals("n", DeviceValidator.parseDexOptFlags("v=n,o=y", "v"));
        assertEquals("y", DeviceValidator.parseDexOptFlags("v=n,o=y", "o"));
        assertEquals("y", DeviceValidator.parseDexOptFlags("v=n, o=y ", "o"));
    }

}
