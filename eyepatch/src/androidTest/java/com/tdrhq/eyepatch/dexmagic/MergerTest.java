// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import com.tdrhq.eyepatch.EyePatchTemporaryFolder;
import com.tdrhq.eyepatch.util.ClassLoaderIntrospector;
import dalvik.system.DexFile;
import java.io.File;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

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
        File file = extractClass(Foo.class);
        assertNotNull(file);
        assertThat(Collections.list(new DexFile(file).entries()),
                   hasItem(Foo.class.getName()));
    }

    static class Foo {
    }
}
