// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.util;

import android.text.TextUtils;
import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.util.ArrayList;
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

    public static ClassLoader clone(ClassLoader classLoader) {
        if (!(classLoader instanceof BaseDexClassLoader)) {
            throw new IllegalArgumentException("needs an android classloader to clone");
        }

        List<String> dexPath = getOriginalDexPath(classLoader);
        String dexPathAsStr = TextUtils.join(":", dexPath);
        PathClassLoader cloned = new PathClassLoader(dexPathAsStr, classLoader.getParent());

        return cloned;
    }
}
