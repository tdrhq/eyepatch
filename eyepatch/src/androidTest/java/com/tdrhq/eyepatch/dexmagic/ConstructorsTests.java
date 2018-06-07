// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import com.tdrhq.eyepatch.runner.EyePatchMockable;
import com.tdrhq.eyepatch.runner.EyePatchTestRunner;
import com.tdrhq.eyepatch.util.Checks;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@EyePatchMockable({
  ConstructorsTests.FooChild.class,
  ConstructorsTests.FooChildWithPrimitive.class,
})
@RunWith(EyePatchTestRunner.class)
public class ConstructorsTests {

    public static ClassHandler createClassHandler(final Class klass) {
        return new ClassHandler() {
            @Override
            public Class getResponsibility() {
                return klass;
            }

            @Override
            public Object handleInvocation(Invocation invocation) {
                if (invocation.getMethod().equals("getNumber")) {
                    return 20;
                }
                if (invocation.getMethod().equals("getOtherNumber")) {
                    return 40;
                }
                return null;
            }
        };

    }

    @Test
    public void testCheckSuperWithConstructor() throws Throwable {
        FooChild child = new FooChild();
        assertEquals("parent constructor should be called",
                     20, child.num);
    }


    @Test
    public void superWithPrimitive() throws Throwable {
        FooChildWithPrimitive child = new FooChildWithPrimitive();
        assertEquals("parent constructor should be called",
                     20, child.num);
        assertEquals("a default 0 should be passed",
                     0, child.mBlah);
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

    public static class FooParentWithPrimitive {
        int mBlah;
        int num = 10;
        public FooParentWithPrimitive(int blah) {
            mBlah = blah;
            num = 20;
        }
    }

    public static class FooChildWithPrimitive extends FooParentWithPrimitive {
        public FooChildWithPrimitive() {
            super(19);
        }
    }

}
