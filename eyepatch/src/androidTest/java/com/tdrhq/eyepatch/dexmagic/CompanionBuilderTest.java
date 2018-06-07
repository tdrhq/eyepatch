package com.tdrhq.eyepatch.dexmagic;

import com.tdrhq.eyepatch.EyePatchTemporaryFolder;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

public class CompanionBuilderTest {
    private CompanionBuilder companionBuilder;
    @Rule
    public EyePatchTemporaryFolder tmpdir = new EyePatchTemporaryFolder();

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
        assertFalse(Modifier.isStatic(method.getModifiers()));
    }

    @Test
    public void testHasNoNonStatic() throws Throwable {
        Class klass = companionBuilder.build(Foo.class, getClass().getClassLoader());
        try {
            klass.getMethod("barNonStat");
            fail("expected exception");
        } catch (NoSuchMethodException e) {
            // expected
        }
    }


    public static class Foo {
        public static String bar() {
            return "car";
        }

        public String barNonStat() {
            return "car";
        }
    }
}
