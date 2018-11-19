package com.tdrhq.eyepatch.dexmagic;

import com.tdrhq.eyepatch.classloader.EyePatchClassLoader;
import com.tdrhq.eyepatch.util.Util;
import dalvik.system.DexFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

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

        File outputFile = dexFileGenerator.generate(realClass);

        if (Util.isJvm()) {
            try {
                return loadForJvm(outputFile, realClass, classLoader);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return loadForAndroid(outputFile, realClass, classLoader);
        }
    }

    private Class loadForAndroid(File outputFile, Class realClass, ClassLoader classLoader) {
        try {
            DexFile dexFile = Util.loadDexFile(outputFile);
            return dexFile.loadClass(realClass.getName(), classLoader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Class loadForJvm(File inputDexFile, Class realClass, ClassLoader classLoader) throws IOException {
        File outputJar = File.createTempFile("eyepatch", ".jar");

        ProcessBuilder pb = new ProcessBuilder(
                getDex2JarPath(),
                "-f",
                "-o", outputJar.toString(),
                inputDexFile.toString());

        Process process = pb.start();
        int ret;
        try {
            ret = process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (ret != 0) {
            throw new RuntimeException("dex2jar failed with exit code: " + ret);
        }

        return loadFromJar(outputJar, realClass.getName(), classLoader);
    }

    Class loadFromJar(File outputJar, String className, ClassLoader _classLoader) throws IOException {
        EyePatchClassLoader classLoader = (EyePatchClassLoader) _classLoader;
        JarFile jar = new JarFile(outputJar);

        ZipEntry entry = jar.getEntry(Util.classToResourceName(className));
        InputStream is = jar.getInputStream(entry);

        byte[] data = new byte[(int) entry.getSize()];
        is.read(data);

        return classLoader.defineClassExposed(className, data, 0, (int) entry.getSize());
    }


    private String getDex2JarPath() {
        return "../dex-tools-2.1-SNAPSHOT/d2j-dex2jar.sh";
    }
}
