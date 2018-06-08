package com.tdrhq.eyepatch.runner;

import com.android.dx.Code;
import com.tdrhq.eyepatch.classloader.EyePatchClassLoader;
import com.tdrhq.eyepatch.dexmagic.ClassHandler;
import com.tdrhq.eyepatch.dexmagic.Invocation;
import com.tdrhq.eyepatch.dexmagic.StaticInvocationHandler;
import dalvik.system.PathClassLoader;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@EyePatchMockable( {
    EyePatchTestRunnerTest.Mockable.class,
    EyePatchTestRunnerTest.AnotherMockable.class
})
@RunWith(EyePatchTestRunner.class)
public class EyePatchTestRunnerTest {

    @ClassHandlerProvider(AnotherMockable.class)
    static ClassHandler createAnotherMockableHandler(final Class klass) {
        return new ClassHandler() {
            @Override
            public Object handleInvocation(Invocation invocation) {
                return "car";
            }

            @Override
            public Class getResponsibility() {
                return klass;
            }
        };
    }

    @After
    public void after() throws Throwable {
        StaticInvocationHandler.setDefaultHandler();
    }

    @Test
    public void testPreconditions() throws Throwable {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testVerifyClassLoader() throws Throwable {
        assertEquals(
                EyePatchClassLoader.class.getName(),
                getClass().getClassLoader().getClass().getName());
    }

    public static class Foo {
    }

    public static class Mockable {
        public String foo() {
            return "car";
        }
    }

    @Test
    public void testVerifyClassLoaderForOtherClass() throws Throwable {
        assertEquals(
                EyePatchClassLoader.class.getName(),
                Foo.class.getClassLoader().getClass().getName());
    }

    @Test
    public void testDexmakerHasInternalClass() throws Throwable {
        assertSame(
                PathClassLoader.class,
                Code.class.getClassLoader().getClass());
    }

    @Test
    public void testEyePatchInternals() throws Throwable {
        assertSame(
                PathClassLoader.class,
                EyePatchTestRunner.class.getClassLoader().getClass());

    }

    @Test
    public void testGetTestClassLoader() throws Throwable {
        assertNotNull(getTestClassLoader());
    }

    private EyePatchClassLoader getTestClassLoader() {
        return (EyePatchClassLoader) getClass().getClassLoader();
    }

    @Test
    public void testGetMockables() throws Throwable {
        assertThat(
                getTestClassLoader().getMockables(),
                containsInAnyOrder(Mockable.class.getName(),
                                   AnotherMockable.class.getName()));
    }

    @Test
    public void testHandlerWithAnnotation() throws Throwable {
        assertEquals("car", AnotherMockable.foo());
    }

    @Test
    public void testCallsToStaticHandler() throws Throwable {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        Mockito.when(handler.handleInvocation(
                             Mockito.any(Invocation.class))).thenReturn("blah");
        StaticInvocationHandler.setHandler(handler);

        assertEquals("blah", new Mockable().foo());
    }

    public static class AnotherMockable {
        public static String foo() {
            return "noteverseen";
        }
    }
}
