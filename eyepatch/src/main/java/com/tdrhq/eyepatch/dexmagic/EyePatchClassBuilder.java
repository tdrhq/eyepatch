package com.tdrhq.eyepatch.dexmagic;

import com.tdrhq.eyepatch.util.Util;
import dalvik.system.DexFile;
import java.io.File;
import java.io.IOException;

public class EyePatchClassBuilder {
    public static final String PRE_CONSTRUCT = "__pre_construct__";
    public static final String CONSTRUCT = "__construct__";

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
        try {
            DexFile dexFile = Util.loadDexFile(dexFileGenerator.generate(realClass));
            return dexFile.loadClass(realClass.getName(), classLoader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
