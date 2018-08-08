package com.tdrhq.eyepatch.dexmagic;

import android.support.annotation.NonNull;
import com.android.dx.Code;
import com.android.dx.Local;
import com.android.dx.TypeId;
import com.tdrhq.eyepatch.EyePatchTemporaryFolder;
import com.tdrhq.eyepatch.iface.Invocation;
import com.tdrhq.eyepatch.util.ClassLoaderIntrospector;
import com.tdrhq.eyepatch.util.SmaliPrinter;
import com.tdrhq.eyepatch.util.Whitebox;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.*;
import org.junit.After;
import org.junit.Test;
import static com.tdrhq.eyepatch.util.Whitebox.arg;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EyePatchClassBuilderTest {
    private EyePatchClassBuilder classBuilder;
    private Dispatcher oldHandler;
    private ClassLoader classLoader = ClassLoaderIntrospector.newChildClassLoader();
    private Class wrappedClass;
    private SmaliPrinter smaliPrinter;

    @Rule
    public EyePatchTemporaryFolder tmpdir = new EyePatchTemporaryFolder();
    private StaticInvocationHandler handler;

    @Before
    public void before() throws Exception {
        smaliPrinter = new SmaliPrinter(tmpdir.newFolder("smali"));
        classBuilder = new EyePatchClassBuilder(tmpdir.getRoot(), new SimpleConstructorGeneratorFactory());
        handler = mock(StaticInvocationHandler.class);
        Dispatcher.setHandler(handler);
    }

    private void setupSmaliPrinter() throws IOException {
        smaliPrinter = new SmaliPrinter(tmpdir.newFolder("smalish"));

        DexFileGenerator.debugPrinter = new DexFileGenerator.DebugPrinter() {
                @Override
                public void print(Class klass, File file) {
                    smaliPrinter.printFromFile(file, klass.getName());
                }
            };

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
        DexFileGenerator.debugPrinter = null;
    }

    @Test
    public void testWrapping() throws Exception {
        wrappedClass = wrapClass(Bar.class);
        Object instance = null;
        String functionName = "foo";
        Invocation expectedInvocation = newInvocation(instance, functionName);

        when(handler.handleInvocation(expectedInvocation))
                .thenReturn("foo2");

        assertEquals("foo2", Whitebox.invokeStatic(wrappedClass, functionName));
    }

    @NonNull
    private Invocation newInvocation(Object instance, String functionName, Whitebox.Arg... args) {
        List<Class> types = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (Whitebox.Arg arg : args) {
            types.add(arg.type);
            values.add(arg.value);
        }

        return new Invocation(
                new GeneratedMethod(wrappedClass, functionName,
                                    types.toArray(new Class[] {})),
                instance,
                values.toArray(new Object[] {}));
    }

    @Test
    public void testHandlerArgs() throws Exception {
        wrappedClass = wrapClass(Bar.class);
        String foo = "foo";
        Invocation expectedInvocation = newInvocation(null, foo);

        when(handler.handleInvocation(expectedInvocation))
                .thenReturn("foo3");

        String car = "car";
        Invocation expectedCarInvocation = newInvocation(null, car);
        when(handler.handleInvocation(expectedCarInvocation))
                .thenReturn("car3");
        assertEquals("foo3", Whitebox.invokeStatic(wrappedClass, foo));
        assertEquals("car3", Whitebox.invokeStatic(wrappedClass, car));
    }

    @Test
    public void testNonStatic() throws Exception {
        wrappedClass = wrapClass(Bar.class);
        Object instance = wrappedClass.newInstance();

        String functionName = "nonStatic";
        Invocation invocation = newInvocation(instance, functionName);

        when(handler.handleInvocation(invocation))
                .thenReturn("foo3");

        assertEquals("foo3", Whitebox.invoke(instance, functionName));
    }

    @Test
    public void testFinalMethod() throws Exception {
        wrappedClass = wrapClass(Bar.class);
        Object instance = wrappedClass.newInstance();

        String functionName = "finalMethod";
        Invocation invocation = newInvocation(instance, functionName);

        when(handler.handleInvocation(invocation))
                .thenReturn("foo3");

        assertEquals("foo3", Whitebox.invoke(instance, functionName));
    }

    @Test
    public void testOtherReturnType() throws Exception {
        wrappedClass = wrapClass(Bar.class);
        Object instance = wrappedClass.newInstance();

        String functionName = "otherReturnType";
        Invocation invocation = newInvocation(instance, functionName);

        when(handler.handleInvocation(invocation))
                .thenReturn(Integer.valueOf(30));

        assertEquals(Integer.valueOf(30), Whitebox.invoke(instance, functionName));
    }

    @Test
    public void testPrimitiveReturnType() throws Exception {

        String functionName = "primitiveReturnType";
        wrappedClass = wrapClass(BarWithPrimitive.class);
        Object instance = wrappedClass.newInstance();

        Invocation invocation = newInvocation(instance, functionName);

        when(handler.handleInvocation(invocation))
                .thenReturn(Integer.valueOf(30));

        assertEquals(30, Whitebox.invoke(instance, functionName));
    }

    @Test
    public void testFloatType() throws Exception {

        String functionName = "floatType";
        wrappedClass = wrapClass(BarWithfloat.class);
        Object instance = wrappedClass.newInstance();

        Invocation invocation = newInvocation(instance, functionName);

        when(handler.handleInvocation(invocation))
                .thenReturn(Float.valueOf(30.0f));

        assertEquals(30.0f, Whitebox.invoke(instance, functionName));
    }

    @Test
    public void testVoidReturn() throws Exception {

        String functionName = "doSomething";
        wrappedClass = wrapClass(BarWithVoid.class);
        Object instance = wrappedClass.newInstance();


        Whitebox.invoke(instance, functionName);
        Invocation invocation = newInvocation(instance, functionName);

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

        String functionName = "doSomething";
        wrappedClass = wrapClass(BarWithArgument.class);
        Object instance = wrappedClass.newInstance();

        Whitebox.invoke(instance, functionName,
                        arg(String.class, "foo"));

        Invocation invocation = newInvocation(
                instance,
                functionName,
                arg(String.class, "foo"));

        verify(handler).handleInvocation(invocation);

    }

    public static class BarWithArgument {
        public void doSomething(String arg) {
            fail("never called");
        }
    }

    @Test
    public void testTwoArgs() throws Exception {

        String functionName = "doSomething";
        wrappedClass = wrapClass(BarWithTwoArgument.class);
        Object instance = wrappedClass.newInstance();

        Whitebox.invoke(instance, functionName, arg(String.class, "foo"), arg(Integer.class, 20));

        Invocation invocation = newInvocation(
                instance,
                functionName,
                arg(String.class, "foo"),
                arg(Integer.class, 20));

        verify(handler).handleInvocation(invocation);

    }

    public static class BarWithTwoArgument {
        public void doSomething(String arg, Integer arg2) {
            fail("never called");
        }
    }


    @Test
    public void testPrimitiveArg() throws Exception {

        String functionName = "doSomething";
        wrappedClass = wrapClass(BarWithPrimitiveArgument.class);
        Object instance = wrappedClass.newInstance();

        Whitebox.invoke(instance, functionName, arg(int.class, 20));

        Invocation invocation = newInvocation(
                instance,
                functionName,
                arg(int.class, 20));

        verify(handler).handleInvocation(invocation);

    }

    public static class BarWithPrimitiveArgument {
        public void doSomething(int arg) {
            fail("never called");
        }
    }

    @Test
    public void testTwoArgsWithPrim() throws Exception {

        String functionName = "doSomething";
        wrappedClass = wrapClass(BarWithTwoArgumentWithPrim.class);
        Object instance = wrappedClass.newInstance();

        Whitebox.invoke(instance,
                        functionName,
                        arg(String.class, "foo"),
                        arg(int.class, 20));

        Invocation invocation = newInvocation(
                instance,
                functionName,
                arg(String.class, "foo"),
                arg(int.class, 20));

        verify(handler).handleInvocation(invocation);

    }

    public static class BarWithTwoArgumentWithPrim {
        public void doSomething(String arg, int arg2) {
            fail("never called");
        }
    }

    @Test
    public void testCallsConstructorWithoutArgs() throws Throwable {

        wrappedClass = wrapClass(Foo.class);
        Object instance = wrappedClass.newInstance();

        Invocation preInvocation = newInvocation(null, "__pre_construct__");

        Invocation invocation = newInvocation(instance, "__construct__");

        verify(handler).handleInvocation(preInvocation);
        verify(handler).handleInvocation(invocation);
    }

    public static class Foo {
    }

    @Test
    public void testCallsConstructorWithArgs() throws Throwable {

        Class<?> classArg = FooWithArg.class;
        wrappedClass = wrapClass(classArg);
        Constructor constructor = wrappedClass.getConstructor(int.class);
        Object instance = constructor.newInstance(20);

        Invocation invocation = newInvocation(
                instance,
                "__construct__",
                arg(int.class, 20));

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
        handler = new StaticInvocationHandler() {
                @Override
                public Object handleInvocation(Invocation invocation) {
                    klass[0] = invocation.getInstanceClass();
                    return "";
                }
            };

        Dispatcher.setHandler(handler);

        wrappedClass = wrapClass(Bar.class);
        Whitebox.invokeStatic(wrappedClass, "foo");
        assertEquals(wrappedClass.getName(), klass[0].getName());
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
        wrappedClass = wrapClass(Foo3.class);
        Invocation expectedInvocation = newInvocation(
                null,
                "bar",
                arg(int.class, 2));

        when(handler.handleInvocation(expectedInvocation))
                .thenReturn("int1");

        Invocation expectedInvocation2 = newInvocation(
                null,
                "bar",
                arg(String.class, "two"));

        when(handler.handleInvocation(expectedInvocation2))
                .thenReturn("String1");


        assertEquals("int1", Whitebox.invokeStatic(wrappedClass, "bar", arg(int.class, 2)));
        assertEquals("String1", Whitebox.invokeStatic(wrappedClass, "bar", arg(String.class, "two")));
    }

    @Test
    public void testUnhandledDefaultHandler() throws Throwable {
        wrappedClass = wrapClass(Foo3.class);
        Object instance = wrappedClass.newInstance();

        when(handler.handleInvocation(newInvocation(
                                              null,
                                              "bar",
                                              arg(int.class, 2))))
                .thenReturn(Dispatcher.UNHANDLED);
        when(handler.handleInvocation(newInvocation(
                                              instance,
                                              "nonStatic")))
                .thenReturn(Dispatcher.UNHANDLED);


        assertEquals("int", Whitebox.invokeStatic(wrappedClass, "bar", arg(int.class, 2)));

        assertEquals("car", Whitebox.invoke(
                             instance, "nonStatic"));

    }

    public static class Foo3 {
        public static String bar(String arg) {
            return "String";
        }
        public static String bar(int arg) {
            return "int";
        }

        public String nonStatic() {
            return "car";
        }
    }

    @Test
    public void testUnhandledDefaultHandlerForPrivateMethod() throws Throwable {
        wrappedClass = wrapClass(Foo3WithPrivate.class);
        Object instance = wrappedClass.newInstance();

        when(handler.handleInvocation(newInvocation(
                                              instance,
                                              "nonStatic")))
                .thenReturn(Dispatcher.UNHANDLED);

        assertEquals("car", Whitebox.invoke(
                             instance, "nonStatic"));

    }

    public static class Foo3WithPrivate {
        private String nonStatic() {
            return "car";
        }
    }


    @Test
    public void testUnhandledDefaultHandlerForVoidMethods() throws Throwable {
        wrappedClass = wrapClass(Foo3WithVoid.class);
        Object instance = wrappedClass.newInstance();

        when(handler.handleInvocation(newInvocation(
                                              null,
                                              "bar",
                                              arg(int.class, 2))))
                .thenReturn(Dispatcher.UNHANDLED);
        when(handler.handleInvocation(newInvocation(
                                              instance,
                                              "nonStatic")))
                .thenReturn(Dispatcher.UNHANDLED);


        Whitebox.invokeStatic(wrappedClass, "bar", arg(int.class, 2));
        assertEquals("kirk", Whitebox.getField(null, wrappedClass, "sField"));

        Whitebox.invoke(instance, "nonStatic");
        assertEquals("aisha", Whitebox.getField(instance, wrappedClass, "field"));

    }

    public static class Foo3WithVoid {
        static String sField = "zoidberg";
        String field = "zoidberg";
        public static void bar(int arg) {
            sField = "kirk";
        }

        public void nonStatic() {
            field = "aisha";
        }
    }

    @Test
    public void testUnhandledConstructor() throws Throwable {
        wrappedClass = wrapClass(FooWithConstructor.class);

        when(handler.handleInvocation(newInvocation(
                                              null,
                                              "__pre_construct__",
                                              arg(int.class, 2))))
                .thenReturn(Dispatcher.UNHANDLED);

        Constructor cons = wrappedClass.getConstructor(int.class);
        Object instance = cons.newInstance(2);

        int num = (int) Whitebox.getField(instance, wrappedClass, "counter");
        assertEquals(3, num);
    }

    public static class FooWithConstructor {
        int counter = 0;
        public FooWithConstructor(int i) {
            counter = i + 1;
        }
    }
}
