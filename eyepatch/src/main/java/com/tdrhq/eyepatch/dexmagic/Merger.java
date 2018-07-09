// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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
        FileOutputStream os = new FileOutputStream(output);
        FileInputStream is = new FileInputStream(template);

        int length;

        byte[] data = new byte[1000];
        while ((length = is.read(data)) > 0) {
            os.write(data, 0, length);
        }

        os.close();
        is.close();
    }
}
