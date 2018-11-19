// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import com.android.dx.Code;
import com.android.dx.Local;
import com.android.dx.TypeId;
import com.tdrhq.eyepatch.classloader.EyePatchClassLoader;
import com.tdrhq.eyepatch.iface.Dispatcher;
import com.tdrhq.eyepatch.iface.GeneratedMethod;
import com.tdrhq.eyepatch.iface.Invocation;
import com.tdrhq.eyepatch.iface.StaticInvocationHandler;
import com.tdrhq.eyepatch.iface.SuperInvocation;
import com.tdrhq.eyepatch.util.Whitebox;
import java.util.ArrayList;
import java.util.List;
import org.junit.*;
import org.junit.After;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static com.tdrhq.eyepatch.util.Whitebox.arg;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EyePatchClassBuilderTest {
    private EyePatchClassBuilder classBuilder;
    private Dispatcher oldHandler;
    private ClassLoader classLoader;
    private Class wrappedClass;

    @Rule
    public TemporaryFolder tmpdir = new TemporaryFolder();
    private StaticInvocationHandler handler;

    @Before
    public void before() throws Exception {
        classBuilder = new EyePatchClassBuilder(tmpdir.getRoot(), new SimpleConstructorGeneratorFactory());
        classLoader = new EyePatchClassLoader(getClass().getClassLoader());
        handler = mock(StaticInvocationHandler.class);
        Dispatcher.setHandler(handler);
    }

    @After
    public void after() throws Throwable {
        Dispatcher.setDefaultHandler();
        DexFileGenerator.debugPrinter = null;
    }

    @Test
    public void testPreconditions() throws Throwable {
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

    private Invocation newInvocation(Object instance, String functionName, Whitebox.Arg... args) {
        List<Class> types = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (Whitebox.Arg arg : args) {
            types.add(arg.type);
            values.add(arg.value);
        }

        return new Invocation(
                GeneratedMethod.create(wrappedClass, functionName,
                                        types.toArray(new Class[] {})),
                instance,
                values.toArray(new Object[] {}));
    }

    private Class wrapClass(Class<?> classArg) {
        return classBuilder.wrapClass(
                    classArg.getName(),
                    getClass().getClassLoader(),
                    classLoader);
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
}
