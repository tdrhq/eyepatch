package com.tdrhq.eyepatch;

import android.app.Instrumentation;
import android.os.Bundle;

import com.tdrhq.eyepatch.classloader.ClassHandlerProvider;
import com.tdrhq.eyepatch.classloader.DefaultClassHandlerProvider;
import com.tdrhq.eyepatch.classloader.EyePatchClassLoader;

/**
 * Provides an Instrumentation that allows overriding any class with
 * EyePatch during a process.
 *
 * You should almost never need this for a regular test, this is an
 * experimental tool.
 */
public class EyepatchInstrumentation extends Instrumentation {
    public void setClassHandlerProvider(DefaultClassHandlerProvider classHandlerProvider) {
        if (classHandlerProvider != null) {
            throw new IllegalStateException("cannot set class handler provider multiple times");
        }
        this.classHandlerProvider = classHandlerProvider;
    }

    private DefaultClassHandlerProvider classHandlerProvider;

    @Override
    public void onCreate(Bundle arguments) {
        if (classHandlerProvider == null) {
            throw new RuntimeException("need to set classHandlerProvider before calling onCreate");
        }
        EyePatchClassLoader classLoader = new EyePatchClassLoader(getClass().getClassLoader());
        classLoader.setClassHandlerProvider(classHandlerProvider);
        SystemClassLoaderHacks.registerSystemClassLoader(
                getTargetContext(),
                classLoader
        );
    }


}
