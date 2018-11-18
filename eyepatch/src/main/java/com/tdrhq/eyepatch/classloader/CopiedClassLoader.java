// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.classloader;

public interface CopiedClassLoader {
    public Class<?> findClass(String name) throws ClassNotFoundException;
}
