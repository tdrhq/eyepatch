// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import org.junit.Before;
import static org.junit.Assert.*;

public class EyePatchClassBuilderTest extends AbstractDispatchableClassBuilderTest {
    @Before
    public void before() throws Exception {
        mEyePatchClassBuilder = new EyePatchClassBuilder(tmpdir.getRoot(), new SimpleConstructorGeneratorFactory());
    }
}
