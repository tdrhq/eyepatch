package com.tdrhq.eyepatch.dexmagic;

import com.android.dx.Code;
import com.android.dx.Local;
import com.android.dx.TypeId;
import com.tdrhq.eyepatch.EyePatchTemporaryFolder;
import com.tdrhq.eyepatch.util.ClassLoaderIntrospector;
import com.tdrhq.eyepatch.util.Whitebox;
import static com.tdrhq.eyepatch.util.Whitebox.arg;
import dalvik.system.PathClassLoader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.hamcrest.Matchers;
import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EyePatchClassBuilderTest {
    private EyePatchClassBuilder classBuilder;
    private Dispatcher oldHandler;
    private ClassLoader classLoader = ClassLoaderIntrospector.newChildClassLoader();

    @Rule
    public EyePatchTemporaryFolder tmpdir = new EyePatchTemporaryFolder();

    @Before
    public void before() throws Exception {
        classBuilder = new EyePatchClassBuilder(tmpdir.getRoot(), new SimpleConstructorGeneratorFactory());
    }

    public static class SimpleConstructorGeneratorFactory extends ConstructorGeneratorFactory {
        @Override
        public ConstructorGenerator newInstance(final TypeId<?> typeId,
                                                final Class parent,
                                                final Local<SuperInvocation> superInvocation,
                                                final Code code) {
            return new ConstructorGenerator(null, null, null, null) {
                @Override
                public void declareLocals() {
                }

                @Override
                public void invokeSuper() {
                    TypeId parentId = TypeId.get(parent);
                    code.invokeDirect(parentId.getConstructor(),
                                      null, code.getThis(typeId));

                }
            };
        }
    }


    @After
    public void after() throws Throwable {
        Dispatcher.setDefaultHandler();
    }

    @Test
    public void testWrapping() throws Exception {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        Dispatcher.setHandler(handler);
        Class barWrapped = wrapClass(Bar.class);
        Invocation expectedInvocation = new Invocation(
                barWrapped,
                null,
                "foo",
                new Class[] {},
                new Object[] {});

        when(handler.handleInvocation(expectedInvocation))
                .thenReturn("foo2");

        assertEquals("foo2", Whitebox.invokeStatic(barWrapped, "foo"));
    }

    @Test
    public void testHandlerArgs() throws Exception {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        Dispatcher.setHandler(handler);


        Class barWrapped = wrapClass(Bar.class);
        Invocation expectedInvocation = new Invocation(
                barWrapped,
                null,
                "foo",
                new Class[] {},
                new Object[] {});

        when(handler.handleInvocation(expectedInvocation))
                .thenReturn("foo3");

        Invocation expectedCarInvocation = new Invocation(
                barWrapped,
                null,
                "car",
                new Class[] {},
                new Object[] {});
        when(handler.handleInvocation(expectedCarInvocation))
                .thenReturn("car3");
        assertEquals("foo3", Whitebox.invokeStatic(barWrapped, "foo"));
        assertEquals("car3", Whitebox.invokeStatic(barWrapped, "car"));
    }

    @Test
    public void testNonStatic() throws Exception {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        Dispatcher.setHandler(handler);

        Class barWrapped = wrapClass(Bar.class);
        Object instance = barWrapped.newInstance();

        Invocation invocation = new Invocation(
                barWrapped,
                instance,
                "nonStatic",
                new Class[] {},
                new Object[] {});

        when(handler.handleInvocation(invocation))
                .thenReturn("foo3");

        Method method = barWrapped.getMethod("nonStatic");
        assertEquals("foo3", Whitebox.invoke(instance, "nonStatic"));
    }

    @Test
    public void testFinalMethod() throws Exception {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        Dispatcher.setHandler(handler);

        Class barWrapped = wrapClass(Bar.class);
        Object instance = barWrapped.newInstance();

        Invocation invocation = new Invocation(
                barWrapped,
                instance,
                "finalMethod",
                new Class[] {},
                new Object[] {});

        when(handler.handleInvocation(invocation))
                .thenReturn("foo3");

        assertEquals("foo3", Whitebox.invoke(instance, "finalMethod"));
    }

    @Test
    public void testOtherReturnType() throws Exception {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        Dispatcher.setHandler(handler);

        Class barWrapped = wrapClass(Bar.class);
        Object instance = barWrapped.newInstance();

        Invocation invocation = new Invocation(
                barWrapped,
                instance,
                "otherReturnType",
                new Class[] {},
                new Object[] {});

        when(handler.handleInvocation(invocation))
                .thenReturn(Integer.valueOf(30));

        assertEquals(Integer.valueOf(30), Whitebox.invoke(instance, "otherReturnType"));
    }

    @Test
    public void testPrimitiveReturnType() throws Exception {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        Dispatcher.setHandler(handler);

        String functionName = "primitiveReturnType";
        Class barWrapped = wrapClass(BarWithPrimitive.class);
        Object instance = barWrapped.newInstance();

        Invocation invocation = new Invocation(
                barWrapped,
                instance,
                functionName,
                new Class[] {},
                new Object[] {});

        when(handler.handleInvocation(invocation))
                .thenReturn(Integer.valueOf(30));

        assertEquals(30, Whitebox.invoke(instance, functionName));
    }

    @Test
    public void testFloatType() throws Exception {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        Dispatcher.setHandler(handler);

        String functionName = "floatType";
        Class barWrapped = wrapClass(BarWithfloat.class);
        Object instance = barWrapped.newInstance();

        Invocation invocation = new Invocation(
                barWrapped,
                instance,
                functionName,
                new Class[] {},
                new Object[] {});

        when(handler.handleInvocation(invocation))
                .thenReturn(Float.valueOf(30.0f));

        assertEquals(30.0f, Whitebox.invoke(instance, functionName));
    }

    @Test
    public void testVoidReturn() throws Exception {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        Dispatcher.setHandler(handler);

        String functionName = "doSomething";
        Class barWrapped = wrapClass(BarWithVoid.class);
        Object instance = barWrapped.newInstance();


        Whitebox.invoke(instance, functionName);
        Invocation invocation = new Invocation(
                barWrapped,
                instance,
                functionName,
                new Class[] {},
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
        Dispatcher.setHandler(handler);

        String functionName = "doSomething";
        Class barWrapped = wrapClass(BarWithArgument.class);
        Object instance = barWrapped.newInstance();

        Whitebox.invoke(instance, functionName,
                        arg(String.class, "foo"));

        Invocation invocation = new Invocation(
                barWrapped,
                instance,
                functionName,
                new Class[] { String.class },
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
        Dispatcher.setHandler(handler);

        String functionName = "doSomething";
        Class barWrapped = wrapClass(BarWithTwoArgument.class);
        Object instance = barWrapped.newInstance();

        Whitebox.invoke(instance, functionName, arg(String.class, "foo"), arg(Integer.class, 20));

        Invocation invocation = new Invocation(
                barWrapped,
                instance,
                functionName,
                new Class[] { String.class, Integer.class },
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
        Dispatcher.setHandler(handler);

        String functionName = "doSomething";
        Class barWrapped = wrapClass(BarWithPrimitiveArgument.class);
        Object instance = barWrapped.newInstance();

        Whitebox.invoke(instance, functionName, arg(int.class, 20));

        Invocation invocation = new Invocation(
                barWrapped,
                instance,
                functionName,
                new Class[] { int.class },
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
        Dispatcher.setHandler(handler);

        String functionName = "doSomething";
        Class barWrapped = wrapClass(BarWithTwoArgumentWithPrim.class);
        Object instance = barWrapped.newInstance();

        Whitebox.invoke(instance,
                        functionName,
                        arg(String.class, "foo"),
                        arg(int.class, 20));

        Invocation invocation = new Invocation(
                barWrapped,
                instance,
                functionName,
                new Class[] { String.class, int.class },
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
        Dispatcher.setHandler(handler);

        Class barWrapped = wrapClass(Foo.class);
        Object instance = barWrapped.newInstance();

        Invocation preInvocation = new Invocation(
                barWrapped,
                null,
                "__pre_construct__",
                new Class[] {},
                new Object[] {});

        Invocation invocation = new Invocation(
                barWrapped,
                instance,
                "__construct__",
                new Class[] {},
                new Object[] {});

        verify(handler).handleInvocation(preInvocation);
        verify(handler).handleInvocation(invocation);
    }

    public static class Foo {
    }

    @Test
    public void testCallsConstructorWithArgs() throws Throwable {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        Dispatcher.setHandler(handler);

        Class<?> classArg = FooWithArg.class;
        Class barWrapped = wrapClass(classArg);
        Constructor constructor = barWrapped.getConstructor(int.class);
        Object instance = constructor.newInstance(20);

        Invocation invocation = new Invocation(
                barWrapped,
                instance,
                "__construct__",
                new Class[] { int.class },
                new Object[] {20});

        verify(handler).handleInvocation(invocation);
    }

    private Class wrapClass(Class<?> classArg) {
        return classBuilder.wrapClass(
                    classArg.getName(),
                    getClass().getClassLoader(),
                    classLoader);
    }

    @Test
    public void testWrappingTheSameClassTwiceIsFine() throws Throwable {
        ClassLoader classLoader2 = new PathClassLoader("", null, getClass().getClassLoader());
        Class barWrapped1 = wrapClass(FooWithArg.class);
        Class barWrapped2 = classBuilder.wrapClass(
                FooWithArg.class.getName(),
                getClass().getClassLoader(),
                classLoader2);
        assertNotNull(barWrapped2);
        assertNotNull(barWrapped1);
        assertNotSame(barWrapped1, barWrapped2);
    }

    @Test
    public void testVerifyClassAndClassLoaderInInvocation() throws Throwable {
        final Class[] klass = new Class[1];
        StaticInvocationHandler handler = new StaticInvocationHandler() {
                @Override
                public Object handleInvocation(Invocation invocation) {
                    klass[0] = invocation.getInstanceClass();
                    return "";
                }
            };

        Dispatcher.setHandler(handler);

        Class barWrapped = wrapClass(Bar.class);
        Whitebox.invokeStatic(barWrapped, "foo");
        assertEquals(barWrapped.getName(), klass[0].getName());
        assertSame(classLoader, klass[0].getClassLoader());
    }

    public static class FooWithArg {
        public FooWithArg(int val) {
        }
    }

    @Test
    public void testCantUseSameClassLoader() throws Throwable {
        try {
            classBuilder.wrapClass(
                    Bar.class.getName(),
                    getClass().getClassLoader(),
                    Bar.class.getClassLoader());
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), Matchers.containsString("different"));
        }
    }

    @Test
    public void testClassExtension() throws Throwable {
        Class fooWrapped = wrapClass(Foo2.class);
        assertNotNull(fooWrapped);

        assertSame(Foo.class, fooWrapped.getSuperclass());
    }

    public static class Foo2 extends Foo {
    }

    @Test
    public void testMethodPolymorph() throws Throwable {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        Dispatcher.setHandler(handler);
        Class barWrapped = wrapClass(Foo3.class);
        Invocation expectedInvocation = new Invocation(
                barWrapped,
                null,
                "bar",
                new Class[] { int.class },
                new Object[] { new Integer(2)});

        when(handler.handleInvocation(expectedInvocation))
                .thenReturn("int1");

        Invocation expectedInvocation2 = new Invocation(
                barWrapped,
                null,
                "bar",
                new Class[] { String.class },
                new Object[] { "two" });

        when(handler.handleInvocation(expectedInvocation2))
                .thenReturn("String1");


        assertEquals("int1", Whitebox.invokeStatic(barWrapped, "bar", arg(int.class, 2)));
        assertEquals("String1", Whitebox.invokeStatic(barWrapped, "bar", arg(String.class, "two")));
    }

    public static class Foo3 {
        public static String bar(String arg) {
            return "String";
        }
        public static String bar(int arg) {
            return "int";
        }
    }
}
