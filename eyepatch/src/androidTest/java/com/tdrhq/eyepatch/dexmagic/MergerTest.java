// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import com.tdrhq.eyepatch.EyePatchTemporaryFolder;
import com.tdrhq.eyepatch.util.ClassLoaderIntrospector;
import java.io.File;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

public class MergerTest {
    @Rule
    public EyePatchTemporaryFolder tmpdir = new EyePatchTemporaryFolder();

    /**
     * Creates a new DexFile with just the given class extracted out.
     */
    private File extractClass(Class klass) {
        File ret = ClassLoaderIntrospector.getDefiningDexFile(klass);
        return ret;
    }

    @Test
    public void testPreconditions() throws Throwable {
        assertNotNull(extractClass(Foo.class));
    }

    static class Foo {
    }
}
