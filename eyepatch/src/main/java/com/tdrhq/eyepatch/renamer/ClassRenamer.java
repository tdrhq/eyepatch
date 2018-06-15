// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.renamer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import com.android.dx.dex.file.DexFile;

/**
 * Generates a dex with all its classes renamed to unique names. WIP.
 */
public class ClassRenamer {
    private File mInput;
    private String mSuffix;

    public ClassRenamer(File input,
                        String suffix) {
        mInput = input;
        mSuffix = suffix;
    }

    public void generate(File output) {
        byte[] buffer = new byte[4096];
        try {
            FileInputStream is = new FileInputStream(mInput);
            FileOutputStream os = new FileOutputStream(output);
            int len;
            while ((len = is.read(buffer)) > 0) {
                os.write(buffer, 0, len);
            }
            os.close();
            is.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
