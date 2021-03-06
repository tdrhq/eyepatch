package com.tdrhq.eyepatch;

import android.content.Context;
import android.os.Build;

import com.tdrhq.eyepatch.util.Whitebox;

public class ClassLoaderHacks {

    public static void validateAppClassLoader(Context context, ClassLoader classLoader) {
        registerAppClassLoader(context, classLoader, true);
    }

    public static void registerAppClassLoader(Context context, ClassLoader classLoader) {
        registerAppClassLoader(context, classLoader, false);
    }

    private static void registerAppClassLoader(Context context, ClassLoader classLoader, boolean validate) {
        ClassLoader expected = classLoader;

        try {
            Class contextImpl = Class.forName("android.app.ContextImpl");

            String loadedApkFieldName = "mPackageInfo";
            String M_CLASS_LOADER = "mClassLoader";
            if (Build.VERSION.SDK_INT >= 26) {

                if (!validate) {
                    if (Whitebox.getField(context, contextImpl, M_CLASS_LOADER) != null) {
                        Whitebox.setField(context, contextImpl, M_CLASS_LOADER, classLoader);
                    }
                }

                ClassLoader actual = (ClassLoader) Whitebox.getField(context, contextImpl, M_CLASS_LOADER);
                if (actual != null && expected != actual) {
                    throw new RuntimeException("ContextImpl/mClassLoader is out of sync");
                }
            }

            if (Build.VERSION.SDK_INT >= 29) {
                loadedApkFieldName = "mLoadedApk";
            }

            Object loadedApk = Whitebox.getField(context, contextImpl, loadedApkFieldName);
            Class loadedApkClass = Class.forName("android.app.LoadedApk");

            if (!validate) {
                Whitebox.setField(loadedApk, loadedApkClass, M_CLASS_LOADER, classLoader);
            }

            ClassLoader actual = (ClassLoader) Whitebox.getField(loadedApk, loadedApkClass, M_CLASS_LOADER);
            if (actual != null && expected != actual) {
                throw new RuntimeException("LoadedApk/mClassLoader is out of sync: " + actual + " vs " + expected);
            }

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }


    }


    private static Class<?> SYSTEM_CLASS_LOADER() {
        try {
            return Class.forName("java.lang.ClassLoader$SystemClassLoader");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
