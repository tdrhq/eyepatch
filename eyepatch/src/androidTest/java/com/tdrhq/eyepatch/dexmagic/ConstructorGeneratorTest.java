package com.tdrhq.eyepatch.dexmagic;

import com.android.dx.Code;
import com.android.dx.DexMaker;
import com.android.dx.Local;
import com.android.dx.TypeId;
import com.tdrhq.eyepatch.EyePatchTemporaryFolder;
import com.tdrhq.eyepatch.util.Checks;
import com.tdrhq.eyepatch.util.ClassLoaderIntrospector;
import dalvik.system.DexFile;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

public class ConstructorGeneratorTest {
    private ClassLoader classLoader = ClassLoaderIntrospector.newChildClassLoader();
    @Rule
    public EyePatchTemporaryFolder tmpdir = new EyePatchTemporaryFolder();

    DexMaker dexmaker;
    TypeId<?> typeId;
    Class superClass;
    Local<SuperInvocation> superInvocation;

    @Before
    public void before() throws Throwable {
        dexmaker = new DexMaker();
        typeId = Util.createTypeIdForName("com.foo.ForTest");
    }

    private void declareClass(Class superClass) {
        this.superClass = superClass;
        dexmaker.declare(
                typeId,
                "com.foo.ForTest.generated",
                Modifier.PUBLIC,
                TypeId.get(superClass));
    }

    @Test
    public void testPreconditions() throws Throwable {
        declareClass(SuperClassSimpleConstructor.class);
        declareConstructor();

        Class klass = generateClass();
        klass.newInstance();
    }

    @Test
    public void testVerifySuperIsCalled() throws Throwable {
        declareClass(SuperClassSimpleConstructor.class);
        declareConstructor();

        Class klass = generateClass();
        SuperClassSimpleConstructor instance = (SuperClassSimpleConstructor) klass.newInstance();
        assertTrue(instance.invoked);
    }

    @Test
    public void testSingleConstructor() throws Throwable {
        declareClass(SuperClassSingleConstructor.class);
        declareConstructor();

        Class klass = generateClass();
        SuperClassSingleConstructor instance = (SuperClassSingleConstructor) klass.newInstance();
        assertTrue(instance.invoked);
        assertEquals("", instance.arg);
    }

    private void declareConstructor() {
        Code code = dexmaker.declare(typeId.getConstructor(), Modifier.PUBLIC);
        superInvocation = code.newLocal(TypeId.get(SuperInvocation.class));
        ConstructorGenerator generator = createGenerator(code);
        generator.declareLocals();
        generator.invokeSuper();
        code.returnVoid();
    }

    private ConstructorGenerator createGenerator(Code code) {
        return new ConstructorGenerator(
                typeId,
                superClass,
                superInvocation,
                code);
    }

    private Class generateClass() throws IOException {
        File of = new File(tmpdir.getRoot(),
                           "MyClass.jar");
        DexFile dexFile = Util.createDexFile(dexmaker, of);
        return Checks.notNull(dexFile.loadClass("com.foo.ForTest", classLoader));
    }

    public static class SuperClassSimpleConstructor {
        boolean invoked = false;
        public SuperClassSimpleConstructor() {
            invoked = true;
        }
    }

    public static class SuperClassSingleConstructor {
        public boolean invoked = false;
        public String arg = null;
        public SuperClassSingleConstructor(String arg) {
            invoked = true;
            this.arg = arg;
        }
    }
}
