package com.tdrhq.eyepatch.dexmagic;

import android.util.Log;
import com.android.dx.Code;
import com.android.dx.Local;
import com.android.dx.TypeId;
import com.tdrhq.eyepatch.EyePatchTemporaryFolder;
import com.tdrhq.eyepatch.util.Checks;
import com.tdrhq.eyepatch.util.ClassLoaderIntrospector;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.hamcrest.Matchers;
import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EyePatchClassBuilderTest {
    private EyePatchClassBuilder mEyePatchClassBuilder;
    private StaticInvocationHandler oldHandler;
    private ClassLoader classLoader = ClassLoaderIntrospector.newChildClassLoader();

    @Rule
    public EyePatchTemporaryFolder tmpdir = new EyePatchTemporaryFolder();

    @Before
    public void before() throws Exception {
        mEyePatchClassBuilder = new EyePatchClassBuilder(tmpdir.getRoot(), new SimpleConstructorGeneratorFactory());
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
        for (String s : tmpdir.getRoot().list()) {
            Log.i("EyePatchClassBuilderTest", "After: " + s);
        }
        StaticInvocationHandler.setDefaultHandler();
    }

    @Test
    public void testWrapping() throws Exception {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        StaticInvocationHandler.setHandler(handler);
        Class barWrapped = mEyePatchClassBuilder.wrapClass(Bar.class, classLoader);
        Invocation expectedInvocation = new Invocation(
                barWrapped,
                null,
                "foo",
                new Class[] {},
                new Object[] {});

        when(handler.handleInvocation(expectedInvocation))
                .thenReturn("foo2");

        Method method = barWrapped.getMethod("foo");
        assertEquals("foo2", method.invoke(null));
    }

    @Test
    public void testHandlerArgs() throws Exception {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        StaticInvocationHandler.setHandler(handler);


        Class barWrapped = mEyePatchClassBuilder.wrapClass(Bar.class, classLoader);
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
                new Class[] {},
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
                new Class[] {},
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
                new Class[] {},
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
                new Class[] {},
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
                new Class[] {},
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
        StaticInvocationHandler.setHandler(handler);

        Class barWrapped = mEyePatchClassBuilder.wrapClass(Foo.class, classLoader);
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
        StaticInvocationHandler.setHandler(handler);

        Class barWrapped = mEyePatchClassBuilder.wrapClass(FooWithArg.class, classLoader);
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

    @Test
    public void testWrappingTheSameClassTwiceIsFine() throws Throwable {
        ClassLoader classLoader2 = new PathClassLoader("", null, getClass().getClassLoader());
        Class barWrapped1 = mEyePatchClassBuilder.wrapClass(FooWithArg.class, classLoader);
        Class barWrapped2 = mEyePatchClassBuilder.wrapClass(FooWithArg.class, classLoader2);
        assertNotNull(barWrapped2);
        assertNotNull(barWrapped1);
        assertNotSame(barWrapped1, barWrapped2);
    }

    @Test
    public void testDexFileIsCached() throws Throwable {
        DexFile file1 = mEyePatchClassBuilder.generateDexFile(Foo.class, getClass().getClassLoader());
        DexFile file2 = mEyePatchClassBuilder.generateDexFile(Foo.class, getClass().getClassLoader());
        assertSame(file1, file2);
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

        StaticInvocationHandler.setHandler(handler);

        Class barWrapped = mEyePatchClassBuilder.wrapClass(Bar.class, classLoader);
        Method method = barWrapped.getMethod("foo");
        method.invoke(null);
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
            mEyePatchClassBuilder.wrapClass(Bar.class, Bar.class.getClassLoader());
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), Matchers.containsString("different"));
        }
    }

    @Test
    public void testClassExtension() throws Throwable {
        Class fooWrapped = mEyePatchClassBuilder.wrapClass(Foo2.class, classLoader);
        assertNotNull(fooWrapped);

        assertSame(Foo.class, fooWrapped.getSuperclass());
    }

    public static class Foo2 extends Foo {
    }

    @Test
    public void testMethodPolymorph() throws Throwable {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        StaticInvocationHandler.setHandler(handler);
        Class barWrapped = mEyePatchClassBuilder.wrapClass(Foo3.class, classLoader);
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


        Method method = barWrapped.getMethod("bar", int.class);
        assertEquals("int1", method.invoke(null, 2));

        Method method2 = barWrapped.getMethod("bar", String.class);
        assertEquals("String1", method2.invoke(null, "two"));
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
