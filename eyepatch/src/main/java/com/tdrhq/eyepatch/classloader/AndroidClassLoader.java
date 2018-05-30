// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.classloader;

import com.tdrhq.eyepatch.util.Whitebox;
import dalvik.system.BaseDexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * A class-loader that's kind of like the build-in Android class
 * loader, with places to hook into it.
 */
public class AndroidClassLoader extends ClassLoader {
    private PathClassLoader parent;

    public AndroidClassLoader(ClassLoader realClassLoader) {
        super(realClassLoader);
        parent = (PathClassLoader) realClassLoader;
    }

    public Class<?> findClass(String name) {
        String path = getOriginalDexPath();
        return getClass(); // lies!
    }

    String getOriginalDexPath() {
        Object dexPathList = Whitebox.getField(parent, BaseDexClassLoader.class, "pathList");
        assert(dexPathList != null);
        Object dexElements = Whitebox.getField(dexPathList, "dexElements");
        assert(dexElements != null);
        return null;
    }

}
