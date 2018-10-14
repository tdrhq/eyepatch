// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.util;

import android.os.Build;
import android.text.TextUtils;
import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClassLoaderIntrospector {
    private ClassLoaderIntrospector() {
    }


    public static List<String> getOriginalDexPath(ClassLoader parent) {
        Object dexPathList = Whitebox.getField(parent, BaseDexClassLoader.class, "pathList");
        assert(dexPathList != null);
        Object[] dexElements = (Object[]) Whitebox.getField(dexPathList, "dexElements");
        assert(dexElements != null);

        List<String> ret = new ArrayList<>();
        for (Object dexElement : dexElements) {
            DexFile file = (DexFile) Whitebox.getField(dexElement, "dexFile");
            if (file != null) {
                ret.add(file.getName());
            }
        }

        return ret;
    }

    // I see this with android.test.mock.jar and
    // org.apache.http.legacy.boot.jar in
    // /system/framework. These are empty jars. At some
    // point I want to understand what this is all about.
    public static boolean isJarToAvoid(String path) {
        return Build.VERSION.SDK_INT >= 27 && path.endsWith(".jar");
    }
    public static File getDefiningDexFile(Class realClass) {
        List<String> dexPath = getOriginalDexPath(realClass.getClassLoader());
        for (String file : dexPath) {
            if (isJarToAvoid(file)) {
                continue;
            }
            try {
                DexFile dexFile = new DexFile(new File(file));
                if (Collections.list(dexFile.entries()).contains(realClass.getName())) {
                    return new File(file);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static String getOriginalDexPathAsStr(ClassLoader parent) {
        return TextUtils.join(":", getOriginalDexPath(parent));
    }

    // useful for tests.
    public static ClassLoader newChildClassLoader() {
        return new PathClassLoader("", null, ClassLoaderIntrospector.class.getClassLoader());
    }


    public static List<String> getOriginalNativeLibPath(ClassLoader parent) {
        Object dexPathList = Whitebox.getField(parent, BaseDexClassLoader.class, "pathList");
        assert(dexPathList != null);

        Object filesVal = Whitebox.getField(dexPathList, "nativeLibraryDirectories");
        List<File> files;

        if (filesVal instanceof List) {
            files = (List<File>) filesVal;
        } else {
            files = Arrays.asList((File[]) filesVal);
        }
        List<String> ret = new ArrayList<>();
        for (File file : files) {
            ret.add(file.getAbsolutePath());
        }
        return ret;
    }

    public static ClassLoader clone(ClassLoader classLoader) {
        try {
            if (!(classLoader instanceof BaseDexClassLoader)) {
                throw new IllegalArgumentException("needs an android classloader to clone");
            }

            Class dexPathList = Class.forName("dalvik.system.DexPathList");
            List<String> dexPath = getOriginalDexPath(classLoader);
            String dexPathAsStr = TextUtils.join(":", dexPath);

            List<String> nativeLibPath = getOriginalNativeLibPath(classLoader);
            String nativeLibPathAsStr = TextUtils.join(":", nativeLibPath);

            PathClassLoader cloned = new PathClassLoader(dexPathAsStr, nativeLibPathAsStr, classLoader.getParent());

            return cloned;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
