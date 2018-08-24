// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.runner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.JUnit4;
import org.junit.runners.model.InitializationError;
import android.support.test.InstrumentationRegistry;

import com.tdrhq.eyepatch.util.DeviceValidator;

/**
 * The main JUnit test runner used for all EyePatch tests.
 *
 * @see {@code EyePatchMockables}
 */
public class EyePatchTestRunner extends Runner {
    private Runner delegate;

    public EyePatchTestRunner(Class<?> testClass) throws InitializationError {
        DeviceValidator.assertDeviceIsAcceptable(InstrumentationRegistry.getTargetContext());
        EyePatchMockable mockableAnnotation = testClass.getAnnotation(EyePatchMockable.class);
        List<Class> mockables = new ArrayList<>();

        mockables.addAll(Arrays.asList(
                                 mockableAnnotation != null ?
                                 mockableAnnotation.value() :
                                 new Class[] {}));

        mockables.addAll(findMockablesFromMethodAnnotations(testClass));

        testClass = TestController.getInstance()
                .generateTestClass(testClass,
                                   mockables.toArray(new Class[] {}),
                                   getClass().getClassLoader());
        delegate = new JUnit4(testClass);
    }

    private Collection<? extends Class> findMockablesFromMethodAnnotations(Class<?> testClass) {
        List<Class> ret = new ArrayList<>();
        for (ClassHandlerProvider provider :
                     TestController.getClassHandlerProviderMap(testClass).values()) {
            ret.add(provider.value());
        }
        return ret;
    }

    @Override
    public Description getDescription() {
        return delegate.getDescription();
    }

    @Override
    public void run(RunNotifier runNotifier) {
        delegate.run(runNotifier);
    }
}
