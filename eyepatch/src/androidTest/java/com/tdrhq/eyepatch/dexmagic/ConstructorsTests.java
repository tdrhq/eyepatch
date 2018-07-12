// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import com.tdrhq.eyepatch.EyePatchTemporaryFolder;
import com.tdrhq.eyepatch.runner.ClassHandlerProvider;
import com.tdrhq.eyepatch.runner.EyePatchMockable;
import com.tdrhq.eyepatch.runner.EyePatchTestRunner;
import com.tdrhq.eyepatch.util.Checks;
import com.tdrhq.eyepatch.util.SmaliPrinter;
import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@EyePatchMockable({
  ConstructorsTests.FooChild.class,
})
@RunWith(EyePatchTestRunner.class)
public class ConstructorsTests {
    @ClassRule
    public static EyePatchTemporaryFolder tmpdir = new EyePatchTemporaryFolder();
    private SmaliPrinter smaliPrinter;

    private void setupSmaliPrinter() throws IOException {
        smaliPrinter = new SmaliPrinter(tmpdir.newFolder("smalish"));

        DexFileGenerator.debugPrinter = new DexFileGenerator.DebugPrinter() {
                @Override
                public void print(Class klass, File file) {
                    smaliPrinter.printFromFile(file, klass.getName());
                }
            };

    }

    @After
      public void after() throws Throwable {
          DexFileGenerator.debugPrinter = null;
      }

    @ClassHandlerProvider(FooChildWithPrimitive.class)
    public static ClassHandler createFooChildWithPrimitive(final Class klass) {
        return new ClassHandler() {
            @Override
            public Class getResponsibility() {
                return klass;
            }

            @Override
            public Object handleInvocation(Invocation invocation) {
                return null;
            }
        };

    }

    @ClassHandlerProvider(FooChildWithBoth.class)
    public static ClassHandler createFooChildWithBoth(final Class klass) {
        return new ClassHandler() {
            @Override
            public Class getResponsibility() {
                return klass;
            }

            @Override
            public Object handleInvocation(Invocation invocation) {
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

    @Test
    public void superWithBoth() throws Throwable {
        FooChildWithBoth child = new FooChildWithBoth();
        assertEquals("parent constructor should be called",
                     20, child.num);
        assertEquals("a default 0 should be passed",
                     0, child.mBlah);

        assertEquals("a default empty should also be passed",
                     "", child.mCar);
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


    public static class FooParentWithBoth {
        int mBlah;
        String mCar;
        int num = 10;
        public FooParentWithBoth(int blah, String car) {
            mBlah = blah;
            mCar = car;
            num = 20;
        }
    }

    public static class FooChildWithBoth extends FooParentWithBoth {
        public FooChildWithBoth() {
            super(19, "notseenever");
        }
    }

}
