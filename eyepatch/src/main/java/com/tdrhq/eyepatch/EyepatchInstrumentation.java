package com.tdrhq.eyepatch;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.Bundle;
import android.util.Log;

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

    private EyePatchClassLoader classLoader;

    public void setClassHandlerProvider(ClassHandlerProvider classHandlerProvider) {
        if (this.classHandlerProvider != null) {
            throw new IllegalStateException("cannot set class handler provider multiple times");
        }
        this.classHandlerProvider = classHandlerProvider;
    }

    public void setClassLoader(EyePatchClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    private ClassHandlerProvider classHandlerProvider;

    @Override
    public void onCreate(Bundle arguments) {
        if (classHandlerProvider == null) {
            throw new RuntimeException("need to set classHandlerProvider before calling onCreate");
        }

        ClassLoader oldClassLoader = getTargetContext().getClassLoader();
        Log.i("EyePatchClassLoader", "Old classloader is: " + oldClassLoader);

        if (classLoader != null) {
            classLoader = new EyePatchClassLoader(oldClassLoader);
            Log.i("EyePatchClassLoader", "New classloader is: " + classLoader);
            classLoader.setClassHandlerProvider(classHandlerProvider);
        }
        ClassLoaderHacks.registerAppClassLoader(
                getTargetContext(),
                classLoader
        );
    }

    @Override
    public void onStart() {
        ClassLoaderHacks.validateAppClassLoader(
                getTargetContext(),
                classLoader
        );
        super.onStart();
    }

    @Override
    public void finish(int resultCode, Bundle results) {
        ClassLoaderHacks.validateAppClassLoader(
                getTargetContext(),
                classLoader
        );
        super.finish(resultCode, results);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        ClassLoaderHacks.validateAppClassLoader(
                getTargetContext(),
                classLoader
        );
        super.callActivityOnCreate(activity, icicle);
    }
}
