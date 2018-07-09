// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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
