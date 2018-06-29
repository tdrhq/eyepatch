// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import org.junit.Before;

public class EyePatchClassBuilderTest extends AbstractDispatchableClassBuilderTest {
    @Before
    public void before() throws Exception {
        classBuilder = new EyePatchClassBuilder(tmpdir.getRoot(), new SimpleConstructorGeneratorFactory());
    }
}
