// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import com.android.dx.Code;
import com.android.dx.Local;
import com.android.dx.TypeId;
import com.tdrhq.eyepatch.iface.Dispatcher;
import com.tdrhq.eyepatch.iface.StaticInvocationHandler;
import com.tdrhq.eyepatch.iface.SuperInvocation;
import com.tdrhq.eyepatch.util.ClassLoaderIntrospector;
import org.junit.*;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EyePatchClassBuilderTest {
    private EyePatchClassBuilder classBuilder;
    private Dispatcher oldHandler;
    private ClassLoader classLoader = ClassLoaderIntrospector.newChildClassLoader();
    private Class wrappedClass;

    @Rule
    public TemporaryFolder tmpdir = new TemporaryFolder();
    private StaticInvocationHandler handler;

    @Before
    public void before() throws Exception {
        classBuilder = new EyePatchClassBuilder(tmpdir.getRoot(), new SimpleConstructorGeneratorFactory());
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
