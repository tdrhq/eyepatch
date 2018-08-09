package com.tdrhq.eyepatch.dexmagic;

import com.android.dx.Code;
import com.android.dx.DexMaker;
import com.android.dx.Local;
import com.android.dx.MethodId;
import com.android.dx.TypeId;
import com.tdrhq.eyepatch.EyePatchTemporaryFolder;
import com.tdrhq.eyepatch.iface.SuperInvocation;
import com.tdrhq.eyepatch.util.Checks;
import com.tdrhq.eyepatch.util.ClassLoaderIntrospector;
import com.tdrhq.eyepatch.util.Util;

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
        declareConstructor(SuperInvocation.empty());

        Class klass = generateClass();
        klass.newInstance();
    }

    @Test
    public void testVerifySuperIsCalled() throws Throwable {
        declareClass(SuperClassSimpleConstructor.class);
        declareConstructor(SuperInvocation.empty());

        Class klass = generateClass();
        SuperClassSimpleConstructor instance = (SuperClassSimpleConstructor) klass.newInstance();
        assertTrue(instance.invoked);
    }

    @Test
    public void testSingleConstructor() throws Throwable {
        declareClass(SuperClassSingleConstructor.class);
        declareConstructor(new SuperInvocation(
                                   new Class[] { String.class },
                                   new String[] { "" }));

        Class klass = generateClass();
        SuperClassSingleConstructor instance = (SuperClassSingleConstructor) klass.newInstance();
        assertTrue(instance.invoked);
        assertEquals("", instance.arg);
    }

    @Test
    public void testMultipleConstructors() throws Throwable {
        declareClass(SuperClassWithMultipleConstructors.class);
        declareConstructor(SuperInvocation.empty());

        Class klass = generateClass();
        SuperClassWithMultipleConstructors instance =
                (SuperClassWithMultipleConstructors) klass.newInstance();
        assertEquals(1, instance.invoked);
    }

    @Test
    public void testManuallyPickingConstructor() throws Throwable {
        declareClass(SuperClassWithMultipleConstructors.class);
        declareConstructor(
                new SuperInvocation(
                        new Class[] { String.class },
                        new Object[] { "foo" }));

        Class klass = generateClass();
        SuperClassWithMultipleConstructors instance =
                (SuperClassWithMultipleConstructors) klass.newInstance();
        assertEquals(2, instance.invoked);
    }

    private void declareConstructor(SuperInvocation expectedSuperInvocation) {
        Code code = dexmaker.declare(typeId.getConstructor(), Modifier.PUBLIC);
        superInvocation = code.newLocal(TypeId.get(SuperInvocation.class));

        Local<Class[]> argTypes = code.newLocal(TypeId.get(Class[].class));
        Local<Object[]> args = code.newLocal(TypeId.get(Object[].class));
        Local<Class> nextArgType = code.newLocal(TypeId.get(Class.class));
        Local<Object> nextArg = code.newLocal(TypeId.OBJECT);
        Local<Integer> arrLength = code.newLocal(TypeId.get(int.class));
        Local<Class> tmpClass = code.newLocal(TypeId.get(Class.class));
        Local<Object> tmpObject = code.newLocal(TypeId.OBJECT);
        Local<Integer> index = code.newLocal(TypeId.INT);

        ConstructorGenerator generator = createGenerator(code);
        generator.declareLocals();

        code.loadConstant(arrLength, expectedSuperInvocation.getArgTypes().length);
        code.newArray(argTypes, arrLength);
        code.loadConstant(arrLength, expectedSuperInvocation.getArgs().length);
        code.newArray(args, arrLength);



        for (int i = 0 ; i < expectedSuperInvocation.getArgTypes().length; i++) {
            code.loadConstant(tmpClass, expectedSuperInvocation.getArgTypes()[i]);
            code.loadConstant(index, i);
            code.aput(argTypes, index, tmpClass);
        }

        for (int i = 0 ; i < expectedSuperInvocation.getArgs().length; i++) {
            code.loadConstant(tmpObject, expectedSuperInvocation.getArgs()[i]);
            code.loadConstant(index, i);
            code.aput(args, index, tmpObject);
        }


        MethodId constructor = TypeId.get(SuperInvocation.class)
                .getConstructor(
                        TypeId.get(Class[].class),
                        TypeId.get(Object[].class));
        code.newInstance(
                superInvocation,
                constructor,
                argTypes,
                args);

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
                           "MyClass.dex");
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

    public static class SuperClassWithMultipleConstructors {
        public int invoked = 0;

        public SuperClassWithMultipleConstructors() {
            invoked = 1;
        }

        public SuperClassWithMultipleConstructors(String arg) {
            invoked = 2;
        }
    }
}
