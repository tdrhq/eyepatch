package com.tdrhq.eyepatch.dexmagic;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class CompanionBuilderTest {
    private CompanionBuilder companionBuilder;
    @Rule
    public TemporaryFolder tmpdir = new TemporaryFolder();

    @Before
    public void before() throws Throwable {
        companionBuilder = new CompanionBuilder(tmpdir.getRoot());
    }

    @Test
    public void testPreconditions() throws Throwable {
        assertNotNull(companionBuilder.build(Foo.class, getClass().getClassLoader()));
    }

    public static class Foo {
        public static String bar() {
            return "car";
        }
    }
}
