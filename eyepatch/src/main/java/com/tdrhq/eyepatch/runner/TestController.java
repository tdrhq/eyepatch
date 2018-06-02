package com.tdrhq.eyepatch.runner;

import com.tdrhq.eyepatch.classloader.EyePatchClassLoader;
import com.tdrhq.eyepatch.dexmagic.CompanionBuilder;
import com.tdrhq.eyepatch.dexmagic.EyePatchClassBuilder;
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
                classLoader1,
                this.classBuilder,
                this.companionBuilder);

        List<String> mockablesStr = new ArrayList<>();
        for (Class klass : mockables) {
            mockablesStr.add(klass.getName());
        }
        classLoader.setMockables(mockablesStr);

        try {
            testClass = classLoader.loadClass(testClass.getName());
        } catch (ClassNotFoundException e) {
            throw new InitializationError(e);
        }
        return testClass;
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
