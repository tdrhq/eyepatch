// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.classloader;

import android.util.Log;
import com.tdrhq.eyepatch.iface.ClassHandler;
import com.tdrhq.eyepatch.dexmagic.DefaultInvocationHandler;
import com.tdrhq.eyepatch.dexmagic.HasStaticInvocationHandler;
import com.tdrhq.eyepatch.dexmagic.StaticInvocationHandler;
import com.tdrhq.eyepatch.iface.StaticVerificationHandler;
import com.tdrhq.eyepatch.util.Checks;
import com.tdrhq.eyepatch.util.ClassLoaderIntrospector;
import com.tdrhq.eyepatch.util.Whitebox;

import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;
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
    implements HasStaticInvocationHandler, StaticVerificationHandler {
    private PathClassLoader parent;
    private StaticInvocationHandler mStaticInvocationHandler;

    List<DexFile> dexFiles = new ArrayList<>();
    Map<String, Class> mockedClasses = new HashMap<>();
    private ClassHandlerProvider classHandlerProvider;


    public EyePatchClassLoader(ClassLoader realClassLoader) {
        super(realClassLoader);
        parent = (PathClassLoader) realClassLoader;
        classHandlerProvider = new ClassHandlerProvider(new ArrayList<ClassHandler>());
    }

    public void setClassHandlers(List<ClassHandler> classHandlers) {
        classHandlerProvider = new ClassHandlerProvider(classHandlers);
        for (ClassHandler classHandler : classHandlerProvider.getClassHandlers()) {
            String className = classHandler.getResponsibility().getName();
            mockedClasses.put(className, classHandler.getResponsibility());
        }
        mStaticInvocationHandler = DefaultInvocationHandler
                .newInstance(classHandlerProvider);
    }

    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (classHandlerProvider.hasClassHandler(name)) {
            return classHandlerProvider.getClassHandler(name).getResponsibility();
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
                name.endsWith("Tests") ||
                name.contains("Tests$") ||
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

    @Override
    public void verifyStatic(Class klass) {
        ClassHandler handler = classHandlerProvider.getClassHandler(klass);
        if (handler.getResponsibility() == klass) {
            if (!(isMockitoClassHandler(handler))) {
                throw new RuntimeException("can't verify against this class");
            }
            Whitebox.invoke(handler, "verifyStatic");
            return;
        }
        throw new IllegalStateException("Could not find handler for: " +
                                        klass.getName());
    }

    @Override
    public void resetStatic(Class klass) {
        ClassHandler handler = classHandlerProvider.getClassHandler(klass);
        if (handler.getResponsibility() == klass) {
            if (!isMockitoClassHandler(handler)) {
                throw new RuntimeException("can't verify against this class");
            }
            Whitebox.invoke(handler, "resetStatic");
            return;
        }

        throw new IllegalStateException("Could not find handler for: " +
                                        klass.getName());
    }

    private boolean isMockitoClassHandler(ClassHandler handler) {
        Class mockitoClassHandler = null;
        try {
            mockitoClassHandler = Class.forName("com.tdrhq.eyepatch.mockito.MockitoClassHandler");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("could not find MockitoClassHandler, this is a bad configuration", e);
        }
        return mockitoClassHandler.isAssignableFrom(handler.getClass());
    }
}
