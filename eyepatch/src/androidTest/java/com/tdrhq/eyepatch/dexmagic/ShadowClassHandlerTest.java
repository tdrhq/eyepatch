package com.tdrhq.eyepatch.dexmagic;

import com.tdrhq.eyepatch.runner.EyePatchMockable;
import com.tdrhq.eyepatch.runner.EyePatchTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@EyePatchMockable({
  ShadowClassHandlerTest.Foo.class,
})
@RunWith(EyePatchTestRunner.class)
public class ShadowClassHandlerTest {

      public static ClassHandler createClassHandler(final Class klass) {
          if (klass.getName().equals(Foo.class.getName())) {
              return new ShadowClassHandler(klass, FooShadow.class);
          }

          throw new RuntimeException("unexpected class");
      }

      @Test
      public void testShadowing() throws Throwable {
          Foo foo = new Foo(20);
          assertEquals(20, foo.number());
      }

      public static class Foo {
          public Foo(int arg) {
          }

          public int number() {
              return -1;
          }
      }

      public static class FooShadow {
          int arg;
          public void __construct__(int arg) {
              this.arg = arg;
          }

          public int number() {
              return this.arg;
          }
      }
}
