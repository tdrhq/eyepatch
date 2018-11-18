// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.classloader;

import com.tdrhq.eyepatch.util.Util;

class JvmCopiedClassLoader implements CopiedClassLoader {
    private EyePatchClassLoader classLoader;
    private ClassLoader parent;

    public JvmCopiedClassLoader(EyePatchClassLoader classLoader,
                                ClassLoader parent) {
        this.classLoader = classLoader;
        System.out.println(parent.getClass());
        this.parent = parent;
    }

    @Override
    public Class<?> findClass(String klass) throws ClassNotFoundException {
        byte[] data = Util.getClassBytes(parent, klass);
        return classLoader.defineClassExposed(klass, data, 0, data.length);
    }

}
