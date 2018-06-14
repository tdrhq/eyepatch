// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.renamer;

import java.io.File;

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
    }
}
