package com.tdrhq.eyepatch.dexmagic;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

    @Test
    public void testIsInterface() throws Throwable {
        Class klass = companionBuilder.build(Foo.class, getClass().getClassLoader());
        // TODO: make this an interface
        assertTrue(Modifier.isAbstract(klass.getModifiers()));
    }

    @Test
    public void testHasMethodBar() throws Throwable {
        Class klass = companionBuilder.build(Foo.class, getClass().getClassLoader());
        Method method = klass.getMethod("bar");
        assertNotNull(method);
    }

    public static class Foo {
        public static String bar() {
            return "car";
        }
    }
}
