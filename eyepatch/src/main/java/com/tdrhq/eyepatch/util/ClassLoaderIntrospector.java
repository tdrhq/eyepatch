// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.util;

import android.text.TextUtils;
import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import static com.tdrhq.eyepatch.util.Whitebox.arg;

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

    public static void addDexPaths(ClassLoader classLoader, List<String> dexPaths) {
        Object dexPathList = Whitebox.getField(classLoader, BaseDexClassLoader.class, "pathList");
        assert(dexPathList != null);

        String dexPath = TextUtils.join(":", dexPaths);
        Whitebox.invoke(dexPathList,
                        "addDexPath",
                        arg(String.class, dexPath),
                        arg(File.class, null));
    }

    public static List<String> getOriginalNativeLibPath(ClassLoader parent) {
        Object dexPathList = Whitebox.getField(parent, BaseDexClassLoader.class, "pathList");
        assert(dexPathList != null);

        List<File> files = (List<File>) Whitebox.getField(dexPathList, "nativeLibraryDirectories");
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
            String zipSeparator = (String) Whitebox.getStaticField(dexPathList, "zipSeparator");
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
