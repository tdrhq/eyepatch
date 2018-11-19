// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.classloader;

import com.tdrhq.eyepatch.iface.ClassHandler;
import com.tdrhq.eyepatch.iface.ClassHandlerProvider;
import com.tdrhq.eyepatch.iface.DefaultClassHandlerProvider;
import com.tdrhq.eyepatch.iface.DefaultInvocationHandler;
import com.tdrhq.eyepatch.iface.HasStaticInvocationHandler;
import com.tdrhq.eyepatch.iface.StaticInvocationHandler;
import com.tdrhq.eyepatch.iface.StaticVerificationHandler;
import com.tdrhq.eyepatch.util.Checks;
import com.tdrhq.eyepatch.util.Whitebox;
import dalvik.system.PathClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * A class-loader that's kind of like the build-in Android class
 * loader, with places to hook into it.
 */
public class EyePatchClassLoader extends ClassLoader
    implements HasStaticInvocationHandler, StaticVerificationHandler {
    private ClassLoader parent;
    private StaticInvocationHandler mStaticInvocationHandler;
    private CopiedClassLoader copiedClassLoader;
    private ClassHandlerProvider classHandlerProvider;


    public EyePatchClassLoader(ClassLoader realClassLoader) {
        super(realClassLoader);
        parent = realClassLoader;
        if (parent instanceof PathClassLoader) {
            copiedClassLoader = new AndroidCopiedClassLoader(this, parent);
        } else {
            copiedClassLoader = new JvmCopiedClassLoader(this, parent);
        }

        classHandlerProvider = new DefaultClassHandlerProvider(new ArrayList<ClassHandler>());
    }

    public void setClassHandlers(List<ClassHandler> classHandlers) {
        DefaultClassHandlerProvider classHandlerProvider = new DefaultClassHandlerProvider(classHandlers);
        setClassHandlerProvider(classHandlerProvider);
    }

    public void setClassHandlerProvider(ClassHandlerProvider classHandlerProvider) {
        this.classHandlerProvider = classHandlerProvider;
        mStaticInvocationHandler = DefaultInvocationHandler
                .newInstance(this.classHandlerProvider);
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

        if (name.startsWith("android.") && !name.startsWith("android.support.")) {
            return true;
        }

        if (name.startsWith("java.")) {
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
                return false;
            }
            return true;
        }

        return false;
    }

    public Class<?> findClass(String name) throws ClassNotFoundException {
        return copiedClassLoader.findClass(name);
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

    public Class<?> defineClassExposed(String name, byte[] data, int start, int end) {
        return super.defineClass(name, data, start, end);
    }
}
