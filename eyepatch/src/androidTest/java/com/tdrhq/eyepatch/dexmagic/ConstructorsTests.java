// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import com.tdrhq.eyepatch.runner.EyePatchMockable;
import com.tdrhq.eyepatch.runner.EyePatchTestRunner;
import com.tdrhq.eyepatch.util.Checks;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@EyePatchMockable({
  FieldsTests.Foo.class,
})
@RunWith(EyePatchTestRunner.class)
public class ConstructorsTests {

    @Test
    public void testCheckSuperWithConstructor() throws Throwable {
        FooChild child = new FooChild();
        assertEquals("parent constructor should be called",
                     20, child.num);
    }

    public static class FooParent {
        String blah = "car";
        int num = 10;
        public FooParent(String blah) {
            this.blah = Checks.notNull(blah);
            num = 20;
        }
    }

    public static class FooChild extends FooParent {
        public FooChild() {
            super("notseen");
        }
    }

}
