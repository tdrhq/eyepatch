// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.util;

import android.text.TextUtils;
import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
