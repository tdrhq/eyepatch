// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.util;

import android.support.test.InstrumentationRegistry;

import org.junit.rules.TemporaryFolder;

public class EyePatchTemporaryFolder extends TemporaryFolder {
    public EyePatchTemporaryFolder() {
        super(InstrumentationRegistry.getTargetContext().getDir("fortest", 0));
    }
}
