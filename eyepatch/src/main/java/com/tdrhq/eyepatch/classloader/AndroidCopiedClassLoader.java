package com.tdrhq.eyepatch.classloader;

import android.util.Log;
import com.tdrhq.eyepatch.util.ClassLoaderIntrospector;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * CopiedClassLoader is not a classLoader per-se, but it tries to
 * provide copies of the same classes that loaded by the parent
 * classloader. Remember, this is *copies*, it doesn't simply delegate
 * to the parent classloader.
 */
class AndroidCopiedClassLoader implements CopiedClassLoader {
    private ClassLoader classLoader;
    private PathClassLoader parent;
    private List<DexFile> dexFiles = new ArrayList<>();

    public AndroidCopiedClassLoader(ClassLoader classLoader, ClassLoader parent) {
        this.classLoader = classLoader;
        this.parent = (PathClassLoader) parent;
    }

    public Class<?> findClass(String name) throws ClassNotFoundException {
        if (dexFiles.size() == 0) {
            try {
                buildDexFiles();
            } catch (IOException e) {
                Log.e("EyePatchClassLoader", "Exception while loading class", e);
                throw new ClassNotFoundException();
            }
        }

        for (DexFile dexFile : dexFiles) {
            Class klass;
            klass = dexFile.loadClass(name, classLoader);
            if (klass != null) {
                return klass;
            }
        }

        throw new ClassNotFoundException(name);
    }

    private void buildDexFiles() throws IOException {
        List<String> path = ClassLoaderIntrospector.getOriginalDexPath(parent);

        for (String file : path) {
            if (ClassLoaderIntrospector.isJarToAvoid(file)) {
                continue;
            }
            dexFiles.add(new DexFile(file));
        }
    }
}
