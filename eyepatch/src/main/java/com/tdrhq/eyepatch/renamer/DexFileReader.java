// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.renamer;

import com.android.dx.dex.file.DexFile;
import com.android.dx.dex.DexOptions;
import java.io.File;

/**
 * Reads a {@code DexFile} from the given file.
 *
 * This lets us modifiy the structures of the DexFile in order to
 * rewrite it as something else.
 */
public class DexFileReader {
    private File file;
    public DexFileReader(File file) {
        this.file = file;
    }

    public DexFile read() {

        DexFile dexFile = new DexFile(new DexOptions());
        return dexFile;
    }
}
