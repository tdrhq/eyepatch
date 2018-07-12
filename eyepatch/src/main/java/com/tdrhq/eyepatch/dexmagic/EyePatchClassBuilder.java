package com.tdrhq.eyepatch.dexmagic;

import android.support.annotation.NonNull;
import com.android.dx.*;
import com.tdrhq.eyepatch.util.Checks;
import dalvik.system.DexFile;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EyePatchClassBuilder {
    public static final String PRE_CONSTRUCT = "__pre_construct__";
    public static final String CONSTRUCT = "__construct__";
    private Map<Key, DexFile> cache = new HashMap<>();

    DexFileGenerator dexFileGenerator;

    public File lastDexFile = null;

    public EyePatchClassBuilder(File dataDir, ConstructorGeneratorFactory constructorGeneratorFactory) {
        dexFileGenerator = new DexFileGenerator(dataDir, constructorGeneratorFactory);
    }

    /**
     * Wraps realClass, to generate a patchable class and loads it
     * into the ClassLoader.
     */
    public Class wrapClass(
            String realClassName,
            ClassLoader originalClassLoader,
            ClassLoader classLoader) {

        Class realClass;
        try {
            realClass = originalClassLoader.loadClass(realClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No such class: " + realClassName, e);
        }
        if (realClass.getClassLoader() == classLoader) {
            throw new IllegalArgumentException(
                    "The classLoader provided must be different from the one " +
                    "used to load realClass");
        }
        DexFile dexFile = generateDexFile(realClass, classLoader);
        return dexFile.loadClass(realClass.getName(), classLoader);

    }

    @NonNull
    private DexFile generateDexFile(Class realClass, ClassLoader classLoader) {
        Key key = new Key(realClass, classLoader);
        if (cache.containsKey(key)) {
            return cache.get(key);
        } else {
            DexFile ret = dexFileGenerator.generate(realClass);
            cache.put(key, ret);
            return ret;
        }
    }

    private static class Key {
        Class klass;
        ClassLoader classLoader;

        public Key(Class klass, ClassLoader classLoader) {
            this.klass = Checks.notNull(klass);
            this.classLoader = Checks.notNull(classLoader);
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Key)) {
                return false;
            }

            Key otherKey = (Key) other;
            return klass == otherKey.klass &&
                    classLoader == otherKey.classLoader;
        }

        @Override
        public int hashCode() {
            return klass.hashCode();
        }
    }
}
