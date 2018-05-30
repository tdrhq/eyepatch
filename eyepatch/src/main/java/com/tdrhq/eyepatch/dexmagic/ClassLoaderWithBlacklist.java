// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import android.util.Log;

public class ClassLoaderWithBlacklist extends ClassLoader {
    ClassLoader parent;
    Class blackList;

    public ClassLoaderWithBlacklist(ClassLoader parent, Class blackList) {
        this.parent = parent;
        this.blackList = blackList;
    }

    public Class loadClass(String className, boolean resolve) throws ClassNotFoundException {
        if (className.equals(blackList.getName())) {
            Log.i("ClassLoaderWithBlacklis", "skipping : " + className);
            throw new ClassNotFoundException();
        }
        Log.i("ClassLoaderWithBlacklis", "Loading original : " + className + "; blacklist: " + blackList.getName());
        return parent.loadClass(className);
    }
}
