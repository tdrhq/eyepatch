// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.classloader;

import android.util.Log;
import com.tdrhq.eyepatch.dexmagic.ClassHandler;
import com.tdrhq.eyepatch.dexmagic.DefaultInvocationHandler;
import com.tdrhq.eyepatch.dexmagic.EyePatchClassBuilder;
import com.tdrhq.eyepatch.dexmagic.HasStaticInvocationHandler;
import com.tdrhq.eyepatch.dexmagic.MockitoClassHandler;
import com.tdrhq.eyepatch.dexmagic.StaticInvocationHandler;
import com.tdrhq.eyepatch.util.Checks;
import com.tdrhq.eyepatch.util.ClassLoaderIntrospector;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class-loader that's kind of like the build-in Android class
 * loader, with places to hook into it.
 */
public class EyePatchClassLoader extends ClassLoader
      implements HasStaticInvocationHandler {
    private PathClassLoader parent;
    private EyePatchClassBuilder classBuilder;
    private StaticInvocationHandler mStaticInvocationHandler;

    List<DexFile> dexFiles = new ArrayList<>();
    Set<String> mockables = new HashSet<>();
    Map<String, Class> mockedClasses = new HashMap<>();


    public EyePatchClassLoader(ClassLoader realClassLoader, EyePatchClassBuilder mockClassBuilder) {
        super(realClassLoader);
        parent = (PathClassLoader) realClassLoader;
        classBuilder = mockClassBuilder;
    }

    public void setMockables(List<String> mockables) {
        this.mockables = new HashSet<>(mockables);

        List<ClassHandler> handlers = new ArrayList<>();
        for (String mockable : mockables) {
            try {
                Class klass = Checks.notNull(
                        classBuilder.wrapClass(
                                getClass().getClassLoader().loadClass(mockable),
                                this));
                mockedClasses.put(mockable, klass);
                handlers.add(new MockitoClassHandler(klass));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        mStaticInvocationHandler = DefaultInvocationHandler
                .newInstance(handlers);
    }

    public Set<String> getMockables() {
        return mockables;
    }

    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (mockedClasses.containsKey(name)) {
            return mockedClasses.get(name);
        }

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

    private boolean isMockable(String name) {
        return mockables.contains(name);
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

        if (name.startsWith("org.mockito")) {
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
                Log.i("EyePatchClassLoader", "Whitelisting: " + name);
                return false;
            }
            return true;
        }

        return false;
    }

    public Class<?> findClass(String name) throws ClassNotFoundException {
        Log.i("EyePatchClassLoader", "totally getting there");
        if (dexFiles.size() == 0) {
            try {
                buildDexFiles();
            } catch (IOException e) {
                Log.e("EyePatchClassLoader", "Exception while loading class", e);
                throw new ClassNotFoundException();
            }
        }

        Log.i("EyePatchClassLoader", "Getting started");
        for (DexFile dexFile : dexFiles) {
            Class klass;
            klass = dexFile.loadClass(name, this);
            if (klass != null) {
                Log.i("EyePatchClassLoader", "found stuff");
                return klass;
            }
        }

        Log.i("EyePatchClassLoader", "did not find the stuff");
        throw new ClassNotFoundException(name);
    }

    private void buildDexFiles() throws IOException {
        List<String> path = ClassLoaderIntrospector.getOriginalDexPath(parent);

        for (String file : path) {
            dexFiles.add(new DexFile(file));
        }
    }

    @Override
    public StaticInvocationHandler getStaticInvocationHandler() {
        return Checks.notNull(mStaticInvocationHandler);
    }
}
