package com.tdrhq.eyepatch.util;

import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import org.jf.baksmali.Baksmali;
import org.jf.baksmali.BaksmaliOptions;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.DexFile;

public class SmaliPrinter {
    private static final String TAG = SmaliPrinter.class.getName();
    private final File dataDir;

    public SmaliPrinter(File dataDir) {
        this.dataDir = new File(dataDir, "smalioutput");
    }

    public void print(byte[] data, String className) {
        init();
        File file = new File(dataDir, "input.dex");

        try {
            OutputStream os = new FileOutputStream((file));
            os.write(data);
            os.close();

            printFileWithoutInit(file, className);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            cleanup();
        }
    }

    public void printOriginalClass(String className) {
        List<String> pathList = ClassLoaderIntrospector.getOriginalDexPath(getClass().getClassLoader());
        for (String path : pathList) {
            if (path.contains("android.test.runner.jar")) {
                continue;
            }
            File file = new File(path);
            printFromFile(file, className);
        }
    }

    public void printFromFile(File file, String className) {
        init();
        try {
            printFileWithoutInit(file, className);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            cleanup();
        }
    }

    private void printFileWithoutInit(File file, String className) throws IOException {
        DexFile dexFile;

        Log.i(TAG, "Trying to read " + file.toString());
        try {
            dexFile = DexFileFactory.loadDexFile(file, Opcodes.getDefault());
        } catch (DexFileFactory.DexFileNotFoundException e) {
            return;
        }
        File output = new File(dataDir, "output");
        output.mkdir();

        Baksmali.disassembleDexFile(dexFile, output, 1, new BaksmaliOptions());

        Log.i(TAG, output.list()[0]);
        StringBuffer buffer = printSmali(className);

        if (buffer == null) {
            Log.i(TAG, "Class " + className + " not found in " + file.toString());
            return;
        }

        Log.i(TAG, "Smali: " + buffer.toString());
    }

    private void init() {
        cleanup();
        dataDir.mkdir();
        Log.i(TAG,  "hello world");
    }

    private void cleanup() {
        if (dataDir.exists()) {
            deleteRecursively(dataDir);
        }
    }

    private void deleteRecursively(File file) {
        if (file.isDirectory()) {
            String[] children = file.list();
            for (String child : children) {
                deleteRecursively(new File(file, child));
            }
        }

        file.delete();
    }

    private StringBuffer printSmali(String className) throws IOException {
        className = className.replace('.', '/');
        className += ".smali";
        File output = new File(new File(dataDir, "output"), className);
        if (!output.exists()) {
            return null;
        }
        InputStreamReader reader = new FileReader(output);
        char[] pdata = new char[100000];
        int len = reader.read(pdata);
        StringBuffer buffer = new StringBuffer();
        buffer.append(pdata, 0, len);
        return buffer;
    }
}
