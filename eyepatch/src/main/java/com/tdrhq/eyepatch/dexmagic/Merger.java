// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import com.tdrhq.eyepatch.util.DexFileUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.writer.io.FileDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

/**
 * Merges a template implementation with a real implementation.
 *
 * This allows the template implementation to dispatch to the
 * Dispatcher, but if the Dispatcher responds by saying that we need
 * to run the original code, then this bypasses to the original
 * implementation.
 */
public class Merger {
    public Merger() {
    }

    public void mergeDexFile(File template, File realCode, File output) throws IOException {
        FileInputStream is = new FileInputStream(template);
        FileInputStream realCodeStream = new FileInputStream(realCode);

        mergeDexFile(is, realCodeStream, output);

        is.close();
        realCodeStream.close();
    }

    public void mergeDexFile(InputStream template, InputStream realCode, File output) throws IOException {
        int length;

        DexBackedDexFile templateDexFile = DexFileUtil.readDexFile(template);

        FileDataStore dataStore = new FileDataStore(output);
        DexPool.writeTo(dataStore, templateDexFile);
        dataStore.close();
    }
}
