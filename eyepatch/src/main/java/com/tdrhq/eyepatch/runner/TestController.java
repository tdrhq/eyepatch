package com.tdrhq.eyepatch.runner;

import android.support.annotation.NonNull;
import com.tdrhq.eyepatch.classloader.EyePatchClassLoader;
import com.tdrhq.eyepatch.dexmagic.ClassHandler;
import com.tdrhq.eyepatch.dexmagic.CompanionBuilder;
import com.tdrhq.eyepatch.dexmagic.ConstructorBuilder;
import com.tdrhq.eyepatch.dexmagic.EyePatchClassBuilder;
import com.tdrhq.eyepatch.dexmagic.MockitoClassHandler;
import com.tdrhq.eyepatch.util.Checks;
import com.tdrhq.eyepatch.util.ExposedTemporaryFolder;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.runners.model.InitializationError;

public class TestController {
    private EyePatchClassBuilder classBuilder;
    private CompanionBuilder companionBuilder;
    private Class<?> originalTestClass;

    private TestController(EyePatchClassBuilder classBuilder,
                           CompanionBuilder companionBuilder) {
        this.classBuilder = classBuilder;
        this.companionBuilder = companionBuilder;
    }

    public Class<?> generateTestClass(Class<?> testClass, Class[] mockables, ClassLoader classLoader1) throws InitializationError {
        this.originalTestClass = Checks.notNull(testClass);
        EyePatchClassLoader classLoader = new EyePatchClassLoader(
                classLoader1
        );

        List<ClassHandler> classHandlers = buildClassHandlers(mockables, classLoader);
        classLoader.setClassHandlers(classHandlers);

        try {
            testClass = classLoader.loadClass(testClass.getName());
        } catch (ClassNotFoundException e) {
            throw new InitializationError(e);
        }
        return testClass;
    }

    @NonNull
    private List<ClassHandler> buildClassHandlers(Class[] mockables, EyePatchClassLoader classLoader) {
        List<String> mockablesStr = new ArrayList<>();
        for (Class klass : mockables) {
            mockablesStr.add(klass.getName());
        }
        List<ClassHandler> classHandlers = new ArrayList<>();
        for (String mockable : mockablesStr) {
            try {
                Class klass = Checks.notNull(
                        buildPatchableClass(classLoader, mockable));
                classHandlers.add(createClassHandler(klass));
            } catch (ClassNotFoundException e1) {
                throw new RuntimeException(e1);
            }
        }
        return classHandlers;
    }

    @NonNull
    private ClassHandler createClassHandler(Class klass) {
        ClassHandler ret;
        try {
            Method method = originalTestClass.getMethod("createClassHandler", Class.class);
            ret = (ClassHandler) method.invoke(null, klass);
        } catch (NoSuchMethodException e) {
            ret = null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        if (ret == null) {
            ret = createFromProviderAnnotation(klass);
        }

        if (ret == null) {
            return new MockitoClassHandler(klass, companionBuilder);
        }
        return ret;
    }

    private ClassHandler createFromProviderAnnotation(Class klass) {
        Method finalMethod = null;
        Map<Method, ClassHandlerProvider> providers = getClassHandlerProviderMap(originalTestClass);
        for (Map.Entry<Method, ClassHandlerProvider> entry : providers.entrySet()) {

            if (entry.getValue().value().getName().equals(klass.getName())) {
                if (finalMethod != null) {
                    throw new RuntimeException("multiple providers for " + klass.getName());
                }
                finalMethod = entry.getKey();
            }

        }

        if (finalMethod != null) {
            try {
                return (ClassHandler) Checks.notNull(finalMethod.invoke(null, klass));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(finalMethod.getName() + " must be public static", e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @NonNull
    static Map<Method, ClassHandlerProvider> getClassHandlerProviderMap(Class originalTestClass) {
        Method[] methods = originalTestClass.getDeclaredMethods();
        Map<Method, ClassHandlerProvider> providers = new HashMap<>();
        for (Method method: methods) {
            ClassHandlerProvider provider =
                    method.getAnnotation(ClassHandlerProvider.class);
            if (provider != null) {
                providers.put(method, provider);
            }
        }
        return providers;
    }

    private Class buildPatchableClass(EyePatchClassLoader classLoader, String className) throws ClassNotFoundException {
        return classBuilder.wrapClass(
                classLoader.getClass().getClassLoader().loadClass(className),
                classLoader);
    }

    volatile static TestController sTestController = null;
    public synchronized static TestController getInstance() {
        if (sTestController == null) {
            ExposedTemporaryFolder tmpdir = new ExposedTemporaryFolder();
            tmpdir.before();
            sTestController = new TestController(
                    new EyePatchClassBuilder(
                            tmpdir.getRoot(),
                            new ConstructorBuilder()),
                    new CompanionBuilder(tmpdir.getRoot()));
        }

        return sTestController;
    }
}
