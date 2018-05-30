// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.classloader;

import android.util.Log;
import com.tdrhq.eyepatch.util.Whitebox;
import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A class-loader that's kind of like the build-in Android class
 * loader, with places to hook into it.
 */
public class AndroidClassLoader extends ClassLoader {
    private PathClassLoader parent;

    List<DexFile> dexFiles = new ArrayList<>();
    Set<String> mockables = new HashSet<>();

    public AndroidClassLoader(ClassLoader realClassLoader) {
        super(realClassLoader);
        parent = (PathClassLoader) realClassLoader;
    }

    public void addMockable(String className) {
        mockables.add(className);
    }

    public Set<String> getMockables() {
        return mockables;
    }

    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (isBlacklisted(name)) {
            return parent.loadClass(name);
        }

        try {
            Class ret = findClass(name);
            resolveClass(ret);
            return ret;
        } catch (ClassNotFoundException e) {
            return parent.loadClass(name);
        }
    }

    /**
     * If anything is blacklisted, all its dependencies *must* be
     * blacklisted too, otherwise bad things can happen.
     */
    private boolean isBlacklisted(String name) {
        if (name.startsWith("org.junit")) {
            return true;
        }

        if (name.startsWith("org.hamcrest")) {
            return true;
        }

        if (name.startsWith("com.android.dx")) {
            return true;
        }

        if (name.startsWith("com.tdrhq.eyepatch.")) {
            // But.. we need to make sure test classes don't get blacklisted
            if (name.endsWith("Test") ||
                name.contains("Test$") ||
                name.contains("Blacklisted")) {
                Log.i("AndroidClassLoader", "Whitelisting: " + name);
                return false;
            }
            return true;
        }

        return false;
    }

    public Class<?> findClass(String name) throws ClassNotFoundException {
        Log.i("AndroidClassLoader", "totally getting there");
        if (dexFiles.size() == 0) {
            try {
                buildDexFiles();
            } catch (IOException e) {
                Log.e("AndroidClassLoader", "Exception while loading class", e);
                throw new ClassNotFoundException();
            }
        }

        Log.i("AndroidClassLoader", "Getting started");
        for (DexFile dexFile : dexFiles) {
            Class klass;
            klass = dexFile.loadClass(name, this);
            if (klass != null) {
                Log.i("AndroidClassLoader", "found stuff");
                return klass;
            }
        }

        Log.i("AndroidClassLoader", "did not find the stuff");
        throw new ClassNotFoundException(name);
    }

    private void buildDexFiles() throws IOException {
        List<String> path = getOriginalDexPath();

        for (String file : path) {
            dexFiles.add(new DexFile(file));
        }
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
