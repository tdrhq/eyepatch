// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.classloader;

import dalvik.system.PathClassLoader;

/**
 * A class-loader that's kind of like the build-in Android class
 * loader, with places to hook into it.
 */
public class AndroidClassLoader extends ClassLoader {
    public AndroidClassLoader(ClassLoader realClassLoader) {
        super(); // no parent
    }

    public Class<?> findClass(String name) {
        return getClass(); // lies!
    }
}
