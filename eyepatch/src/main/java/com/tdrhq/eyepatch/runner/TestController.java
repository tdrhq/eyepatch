package com.tdrhq.eyepatch.runner;

import android.support.annotation.NonNull;

import com.tdrhq.eyepatch.classloader.EyePatchClassLoader;
import com.tdrhq.eyepatch.dexmagic.ClassHandler;
import com.tdrhq.eyepatch.dexmagic.CompanionBuilder;
import com.tdrhq.eyepatch.dexmagic.EyePatchClassBuilder;
import com.tdrhq.eyepatch.dexmagic.MockitoClassHandler;
import com.tdrhq.eyepatch.util.Checks;
import com.tdrhq.eyepatch.util.ExposedTemporaryFolder;
import java.util.ArrayList;
import java.util.List;
import org.junit.runners.model.InitializationError;

public class TestController {
    private EyePatchClassBuilder classBuilder;
    private CompanionBuilder companionBuilder;

    private TestController(EyePatchClassBuilder classBuilder,
                           CompanionBuilder companionBuilder) {
        this.classBuilder = classBuilder;
        this.companionBuilder = companionBuilder;
    }

    public Class<?> generateTestClass(Class<?> testClass, Class[] mockables, ClassLoader classLoader1) throws InitializationError {
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
        return new MockitoClassHandler(klass, companionBuilder);
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
                            tmpdir.getRoot()),
                    new CompanionBuilder(tmpdir.getRoot()));
        }

        return sTestController;
    }
}
