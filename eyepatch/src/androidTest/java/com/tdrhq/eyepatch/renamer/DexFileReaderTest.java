// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.renamer;

import com.android.dx.Code;
import com.android.dx.DexMaker;
import com.android.dx.Local;
import com.android.dx.MethodId;
import com.android.dx.TypeId;
import com.tdrhq.eyepatch.EyePatchTemporaryFolder;
import com.tdrhq.eyepatch.dexmagic.Util;
import com.tdrhq.eyepatch.util.Whitebox;
import com.android.dx.dex.file.DexFile;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.lang.reflect.Modifier;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

public class DexFileReaderTest {
    @Rule
    public EyePatchTemporaryFolder tmpdir = new EyePatchTemporaryFolder();
    private File input;
    private File staticInput;

    private ClassLoader classLoader = new PathClassLoader("", null, getClass().getClassLoader());
    private ClassRenamer classRenamer;
    private File output;

    @Before
    public void before() throws Throwable {
        input = tmpdir.newFile("input.dex");

        // let's write a very basic Class to the file
        DexMaker dexmaker = new DexMaker();
        TypeId<?> typeId = Util.createTypeIdForName("com.foo.Foo");
        dexmaker.declare(typeId, "Foo.generated", Modifier.PUBLIC,
                         TypeId.OBJECT);
        // let's generate a bar() method that returns a constant
        MethodId method = typeId.getMethod(
                TypeId.STRING,
                "getBar");
        Code code = dexmaker.declare(method, Modifier.PUBLIC | Modifier.STATIC);
        Local<String> dummy = code.newLocal(TypeId.STRING);
        Local<String> ret = code.newLocal(TypeId.STRING);
        code.loadConstant(ret, "zoidberg");
        code.returnValue(ret);

        Util.writeDexFile(dexmaker, input);
        classRenamer = new ClassRenamer(input, "suffix");

        staticInput = tmpdir.newFile("static_input.dex");
        StaticDexProvider.writeToFile(staticInput);
    }

    @Test
    public void testPreconditions() throws Throwable {
        // load the original class
        Class FooClass = Util.loadDexFile(input)
                .loadClass("com.foo.Foo", classLoader);
        assertNotNull(FooClass);
        assertEquals("zoidberg", Whitebox.invokeStatic(FooClass, "getBar"));
    }

    @Test
    public void testReadDexFile() throws Throwable {
        DexFile inputFile = new DexFileReader(input).read();
        assertNotNull(inputFile);
    }

    @Test
    public void testReadStaticInput() throws Throwable {
        DexFileReader reader = new DexFileReader(staticInput);
        reader.read();
        assertEquals(7, reader.headerItem.stringIdsSize);
        assertEquals(0x70, reader.headerItem.stringIdsOff);
    }
}
