// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.classloader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
        String resource = klass.replace(".", "/") + ".class";
        System.out.println("finding "+ resource);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            InputStream stream = parent.getResourceAsStream(resource);
            byte[] buff = new byte[2048];

            int len;
            while ((len = stream.read(buff)) > 0) {
                os.write(buff, 0, len);
            }
        } catch (IOException e) {
            throw new ClassNotFoundException("could not find class", e);
        }

        byte[] data = os.toByteArray();
        return classLoader.defineClassExposed(klass, data, 0, data.length);
    }
}
