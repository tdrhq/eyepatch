// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.classloader;

import com.tdrhq.eyepatch.util.Whitebox;
import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
        List<String> path = getOriginalDexPath();
        return getClass(); // lies!
    }

    List<String> getOriginalDexPath() {
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

}
