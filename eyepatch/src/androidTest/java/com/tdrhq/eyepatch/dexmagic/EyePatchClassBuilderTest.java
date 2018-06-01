package com.tdrhq.eyepatch.dexmagic;

import android.util.Log;
import dalvik.system.PathClassLoader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.junit.*;
import org.junit.rules.*;
import static org.junit.Assert.*;
import static org.mockito.AdditionalMatchers.*;
import static org.mockito.Mockito.*;
import org.hamcrest.Matchers;

public class EyePatchClassBuilderTest {
    private EyePatchClassBuilder mEyePatchClassBuilder;
    private StaticInvocationHandler oldHandler;
    private ClassLoader classLoader = new PathClassLoader("", null, getClass().getClassLoader());

    @Rule
    public TemporaryFolder tmpdir = new TemporaryFolder();

    @Before
    public void before() throws Exception {
        mEyePatchClassBuilder = new EyePatchClassBuilder(tmpdir.getRoot());
    }


    @After
    public void after() throws Throwable {
        for (String s : tmpdir.getRoot().list()) {
            Log.i("EyePatchClassBuilderTest", "After: " + s);
        }
        StaticInvocationHandler.setDefaultHandler();
    }

    @Test
    public void testWrapping() throws Exception {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        StaticInvocationHandler.setHandler(handler);
        Invocation expectedInvocation = new Invocation(
                Bar.class,
                null,
                "foo",
                new Object[] {});

        when(handler.handleInvocation(expectedInvocation))
                .thenReturn("foo2");

        Class barWrapped = mEyePatchClassBuilder.wrapClass(Bar.class, classLoader);
        Method method = barWrapped.getMethod("foo");
        assertEquals("foo2", method.invoke(null));
    }

    @Test
    public void testHandlerArgs() throws Exception {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        StaticInvocationHandler.setHandler(handler);

        Invocation expectedInvocation = new Invocation(
                Bar.class,
                null,
                "foo",
                new Object[] {});

        when(handler.handleInvocation(expectedInvocation))
                .thenReturn("foo3");

        Invocation expectedCarInvocation = new Invocation(
                Bar.class,
                null,
                "car",
                new Object[] {});
        when(handler.handleInvocation(expectedCarInvocation))
                .thenReturn("car3");

        Class barWrapped = mEyePatchClassBuilder.wrapClass(Bar.class, classLoader);
        Method method = barWrapped.getMethod("foo");
        assertEquals("foo3", method.invoke(null));

        method = barWrapped.getMethod("car");
        assertEquals("car3", method.invoke(null));
    }

    @Test
    public void testNonStatic() throws Exception {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        StaticInvocationHandler.setHandler(handler);

        Class barWrapped = mEyePatchClassBuilder.wrapClass(Bar.class, classLoader);
        Object instance = barWrapped.newInstance();

        Invocation invocation = new Invocation(
                barWrapped,
                instance,
                "nonStatic",
                new Object[] {});

        when(handler.handleInvocation(invocation))
                .thenReturn("foo3");

        Method method = barWrapped.getMethod("nonStatic");
        assertEquals("foo3", method.invoke(instance));
    }

    @Test
    public void testFinalMethod() throws Exception {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        StaticInvocationHandler.setHandler(handler);

        Class barWrapped = mEyePatchClassBuilder.wrapClass(Bar.class, classLoader);
        Object instance = barWrapped.newInstance();

        Invocation invocation = new Invocation(
                barWrapped,
                instance,
                "finalMethod",
                new Object[] {});

        when(handler.handleInvocation(invocation))
                .thenReturn("foo3");

        Method method = barWrapped.getMethod("finalMethod");
        assertEquals("foo3", method.invoke(instance));
    }

    @Test
    public void testOtherReturnType() throws Exception {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        StaticInvocationHandler.setHandler(handler);

        Class barWrapped = mEyePatchClassBuilder.wrapClass(Bar.class, classLoader);
        Object instance = barWrapped.newInstance();

        Invocation invocation = new Invocation(
                barWrapped,
                instance,
                "otherReturnType",
                new Object[] {});

        when(handler.handleInvocation(invocation))
                .thenReturn(Integer.valueOf(30));

        Method method = barWrapped.getMethod("otherReturnType");
        assertEquals(Integer.valueOf(30), method.invoke(instance));
    }

    @Test
    public void testPrimitiveReturnType() throws Exception {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        StaticInvocationHandler.setHandler(handler);

        String functionName = "primitiveReturnType";
        Class barWrapped = mEyePatchClassBuilder.wrapClass(BarWithPrimitive.class, classLoader);
        Object instance = barWrapped.newInstance();

        Invocation invocation = new Invocation(
                barWrapped,
                instance,
                functionName,
                new Object[] {});

        when(handler.handleInvocation(invocation))
                .thenReturn(Integer.valueOf(30));

        Method method = barWrapped.getMethod(functionName);
        assertEquals(30, method.invoke(instance));
    }

    @Test
    public void testFloatType() throws Exception {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        StaticInvocationHandler.setHandler(handler);

        String functionName = "floatType";
        Class barWrapped = mEyePatchClassBuilder.wrapClass(BarWithfloat.class, classLoader);
        Object instance = barWrapped.newInstance();

        Invocation invocation = new Invocation(
                barWrapped,
                instance,
                functionName,
                new Object[] {});

        when(handler.handleInvocation(invocation))
                .thenReturn(Float.valueOf(30.0f));

        Method method = barWrapped.getMethod(functionName);
        assertEquals(30.0f, method.invoke(instance));
    }

    @Test
    public void testVoidReturn() throws Exception {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        StaticInvocationHandler.setHandler(handler);

        String functionName = "doSomething";
        Class barWrapped = mEyePatchClassBuilder.wrapClass(BarWithVoid.class, classLoader);
        Object instance = barWrapped.newInstance();


        Method method = barWrapped.getMethod(functionName);
        method.invoke(instance);
        Invocation invocation = new Invocation(
                barWrapped,
                instance,
                functionName,
                new Object[] {});

        verify(handler).handleInvocation(invocation);

    }


    public static class Bar {
        public static String foo() {
            return "foot";
        }

        public static String car() {
            return "foot";
        }

        public String nonStatic() {
            return "zoidberg";
        }

        public final String finalMethod() {
            return "zoidberg";
        }

        public final Integer otherReturnType() {
            return 20;
        }
    }

    public static class BarWithPrimitive {
        public int primitiveReturnType() {
            return 20;
        }
    }

    public static class BarWithfloat {
        public float floatType() {
            return 20.0f;
        }
    }


    public static class BarWithVoid {
        public void doSomething() {
        }
    }

    @Test
    public void testSingleArg() throws Exception {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        StaticInvocationHandler.setHandler(handler);

        String functionName = "doSomething";
        Class barWrapped = mEyePatchClassBuilder.wrapClass(BarWithArgument.class, classLoader);
        Object instance = barWrapped.newInstance();


        Method method = barWrapped.getDeclaredMethod(functionName, String.class);
        method.invoke(instance, "foo");

        Invocation invocation = new Invocation(
                barWrapped,
                instance,
                functionName,
                new Object[] {"foo"});

        verify(handler).handleInvocation(invocation);

    }

    public static class BarWithArgument {
        public void doSomething(String arg) {
            fail("never called");
        }
    }

    @Test
    public void testTwoArgs() throws Exception {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        StaticInvocationHandler.setHandler(handler);

        String functionName = "doSomething";
        Class barWrapped = mEyePatchClassBuilder.wrapClass(BarWithTwoArgument.class, classLoader);
        Object instance = barWrapped.newInstance();


        Method method = barWrapped.getDeclaredMethod(functionName, String.class, Integer.class);
        method.invoke(instance, "foo", new Integer(20));

        Invocation invocation = new Invocation(
                barWrapped,
                instance,
                functionName,
                new Object[] {"foo", new Integer(20) });

        verify(handler).handleInvocation(invocation);

    }

    public static class BarWithTwoArgument {
        public void doSomething(String arg, Integer arg2) {
            fail("never called");
        }
    }


    @Test
    public void testPrimitiveArg() throws Exception {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        StaticInvocationHandler.setHandler(handler);

        String functionName = "doSomething";
        Class barWrapped = mEyePatchClassBuilder.wrapClass(BarWithPrimitiveArgument.class, classLoader);
        Object instance = barWrapped.newInstance();


        Method method = barWrapped.getDeclaredMethod(functionName, int.class);
        method.invoke(instance, 20);

        Invocation invocation = new Invocation(
                barWrapped,
                instance,
                functionName,
                new Integer[] {20});

        verify(handler).handleInvocation(invocation);

    }

    public static class BarWithPrimitiveArgument {
        public void doSomething(int arg) {
            fail("never called");
        }
    }

    @Test
    public void testTwoArgsWithPrim() throws Exception {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        StaticInvocationHandler.setHandler(handler);

        String functionName = "doSomething";
        Class barWrapped = mEyePatchClassBuilder.wrapClass(BarWithTwoArgumentWithPrim.class, classLoader);
        Object instance = barWrapped.newInstance();


        Method method = barWrapped.getDeclaredMethod(functionName, String.class, int.class);
        method.invoke(instance, "foo", 20);

        Invocation invocation = new Invocation(
                barWrapped,
                instance,
                functionName,
                new Object[] {"foo", new Integer(20)});

        verify(handler).handleInvocation(invocation);

    }

    public static class BarWithTwoArgumentWithPrim {
        public void doSomething(String arg, int arg2) {
            fail("never called");
        }
    }

    @Test
    public void testCallsConstructorWithoutArgs() throws Throwable {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        StaticInvocationHandler.setHandler(handler);

        Class barWrapped = mEyePatchClassBuilder.wrapClass(Foo.class, classLoader);
        Object instance = barWrapped.newInstance();

        Invocation invocation = new Invocation(
                barWrapped,
                instance,
                "__construct__",
                new Object[] {});

        verify(handler).handleInvocation(invocation);
    }

    public static class Foo {
    }

    @Test
    public void testCallsConstructorWithArgs() throws Throwable {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        StaticInvocationHandler.setHandler(handler);

        Class barWrapped = mEyePatchClassBuilder.wrapClass(FooWithArg.class, classLoader);
        Constructor constructor = barWrapped.getConstructor(int.class);
        Object instance = constructor.newInstance(20);

        Invocation invocation = new Invocation(
                barWrapped,
                instance,
                "__construct__",
                new Object[] {20});

        verify(handler).handleInvocation(invocation);
    }

    public static class FooWithArg {
        public FooWithArg(int val) {
        }
    }

    @Test
    public void testCantUseSameClassLoader() throws Throwable {
        try {
            mEyePatchClassBuilder.wrapClass(Bar.class, Bar.class.getClassLoader());
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), Matchers.containsString("different"));
        }
    }
}