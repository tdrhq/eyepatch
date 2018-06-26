// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.renamer;

import com.android.dx.Code;
import com.android.dx.DexMaker;
import com.android.dx.Local;
import com.android.dx.MethodId;
import com.android.dx.TypeId;
import com.android.dx.dex.file.ClassDefItem;
import com.android.dx.dex.file.DexFile;
import com.tdrhq.eyepatch.EyePatchTemporaryFolder;
import com.tdrhq.eyepatch.dexmagic.Util;
import com.tdrhq.eyepatch.util.Whitebox;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;
import com.tdrhq.eyepatch.renamer.DexFileReader.*;

public class DexFileReaderTest {
    @Rule
    public EyePatchTemporaryFolder tmpdir = new EyePatchTemporaryFolder();
    private File input;
    private File staticInput;
    private NameProvider nameProvider = new NameProvider() {
            @Override
            public String rename(String input) {
                return input.replace(";", "_suffix;");
            }
        };



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
    public void testReadStaticInput() throws Throwable {
        DexFileReader reader = new DexFileReader(staticInput, nameProvider);
        reader.read();
        assertEquals(7, reader.headerItem.stringIdsSize);
        assertEquals(0x70, reader.headerItem.stringIdsOff);
    }

    @Test
    public void testStringIdItem() throws Throwable {
        DexFileReader reader = new DexFileReader(staticInput, nameProvider);
        reader.read();
        assertEquals(0xe4, reader.stringIdItems[0].stringDataOff);
        assertEquals("Foo.generated", reader.stringIdItems[0].getString());
        assertEquals("L", reader.stringIdItems[1].getString());
        assertEquals("Lcom/foo/Foo;", reader.stringIdItems[2].getString());
        assertEquals("Ljava/lang/Object;", reader.stringIdItems[3].getString());
        assertEquals("Ljava/lang/String;", reader.stringIdItems[4].getString());
        assertEquals("getBar", reader.stringIdItems[5].getString());
        assertEquals("zoidberg", reader.stringIdItems[6].getString());
    }

    @Test
    public void testClassDefsData() throws Throwable {
        DexFileReader reader = new DexFileReader(staticInput, nameProvider);
        reader.read();
        assertEquals(1, reader.headerItem.classDefsSize);
        assertEquals(0xac, reader.headerItem.classDefsOff);
        ClassDefItem item = reader.classDefItems[0].toClassDefItem();
        //assertEquals("Lcom/foo/Foo_suffix;", item.getThisClass().toString());
    }

    @Test
    public void testHasClass() throws Throwable {
        DexFileReader reader = new DexFileReader(staticInput, nameProvider);
        output = tmpdir.newFile("output.dex");
        reader.read();
        reader.write(output);

        Class FooClass = Util.loadDexFile(output)
                .loadClass("com.foo.Foo_suffix", classLoader);
        assertNotNull(FooClass);
    }

    @Test
    public void testCodeItemBasics() throws Throwable {
        DexFileReader reader = new DexFileReader(staticInput, nameProvider);
        reader.read();
        _CodeItem codeItem = reader.classDefItems[0].classDataItem.directMethods[0].codeItem;
        assertEquals(1, codeItem.registersSize);
        assertEquals(3, codeItem.insnsSize);


        // const-string, register 0
        assertEquals(0x001a, codeItem.insns[0]);

        // argument to const-string, index 6, see testStringIdItem, this points to "zoidberg"
        assertEquals(0x0006, codeItem.insns[1]);

        // return-object, register 0 insn
        assertEquals(0x0011, codeItem.insns[2]);
    }
}
