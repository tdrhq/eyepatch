# EyePatch

EyePatch let's you mock static methods in Android tests
(a.k.a instrumentation tests, tests that run on an actual
emulator.). This is highly alpha quality at the moment, there may be
edge cases like specific API levels, or class configurations where
this doesn't work.

Essentially this is (a subset of) PowerMock for Android.

## Examples

The best example to get started with is in our own tests: https://github.com/tdrhq/eyepatch/blob/master/eyepatch/src/androidTest/java/com/tdrhq/eyepatch/ExampleIntegrationTest.java

But let's explain that in a bit more detail.

Let's assume you want to mock out the interaction of a static
method. (There are ways to also mock out final methods, and
constructors, and just about anything, but that gets a little
complication and we're still working on making that better.)

So suppose you have a static class like so:
```java
public class StaticClass {
  public String doExpensiveStuff(String input) {
    // .. does a lot of stuff, hits network and what not.
  }
}
```

You have another class that uses this static method:

```java
public class OtherClass {
  public String doOtherStuff() {
     return "output: " + StaticClass.doExpensiveStuff("foo")
  }
}
```

So now you want to write a test for
`OtherClass#doOtherStuff`. Ideally, you refactor the code. But maybe
you're working with some stubborn engineers who really like their
static functions for "performance reasons". Uggh. But let's test
`doOtherStuff` with EyePatch.

```java
@EyePatchMockables({ OtherClass.class })
@RunWith(EyePatchTestRunner.class)
public class OtherClassTest {
  public void test() {
    when(StaticClass.doExpensiveStuff("foo")).thenReturn("blah");
    assertEquals("output: blah", OtherClass.doOtherStuff());
  }
}
```


## Limitations

Many of these limitations are temporary, and we can eventually work
around them:

1. You can't mock Android internal classes as of now. When we do build
this, it's most likely going to have a slightly different API.

2. You can't *spy* on classes. That means, you have to mock out all
the methods on a class.

3. There's support for mocking out final methods, but this isn't
exposed as of yet. Hopefully I'll release this real quick. However,
apparently the latest Mockito version does already support this on
Android P. I haven't tried this myself. (See
https://github.com/linkedin/dexmaker/issues/77)

## API level support

Currently only API level >= 21 is supported, we're working on bringing
this all the way back to 14.

You need to have `android:vmSafeMode="true"` in your `<application>`
tag in your `AndroidManifest.xml`. Otherwise your tests will fail in
mysterious ways (In particular I've seen it failing on API level 22).

## Author

Arnold Noronha <arnold@tdrhq.com>

## License

MIT license, see LICENSE file