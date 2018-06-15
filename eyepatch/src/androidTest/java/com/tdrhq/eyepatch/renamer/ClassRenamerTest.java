// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.renamer;

import android.util.Log;
import com.android.dx.Code;
import com.android.dx.DexMaker;
import com.android.dx.Local;
import com.android.dx.MethodId;
import com.android.dx.TypeId;
import com.tdrhq.eyepatch.EyePatchTemporaryFolder;
import com.tdrhq.eyepatch.dexmagic.Util;
import com.tdrhq.eyepatch.util.Whitebox;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

public class ClassRenamerTest {
    @Rule
    public EyePatchTemporaryFolder tmpdir = new EyePatchTemporaryFolder();
    private File input;
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
    }

    @Test
    public void testPreconditions() throws Throwable {
        // load the original class
        Class FooClass = Util.loadDexFile(input)
                .loadClass("com.foo.Foo", classLoader);
        assertNotNull(FooClass);
        assertEquals("zoidberg", Whitebox.invokeStatic(FooClass, "getBar"));

        hexDump(input);
    }

    @Test
    public void testOutputGeneratesSomethingThatCanbeLoaded() throws Throwable {
        output = tmpdir.newFile("output.dex");
        classRenamer.generate(output);
        Class FooClass = Util.loadDexFile(output)
                .loadClass("com.foo.Foo", classLoader);
        assertNotNull(FooClass);
        assertEquals("zoidberg", Whitebox.invokeStatic(FooClass, "getBar"));
    }

    private void hexDump(File input) throws IOException {
        FileInputStream is = new FileInputStream(input);
        byte[] bytes = new byte[8];

        int len = 0;
        int pos = 0;
        while ((len = is.read(bytes)) > 0) {
            hexDumpPrintLine(bytes, len, pos);
            pos += len;
        }
    }

    private void hexDumpPrintLine(byte[] bytes, int len, int startPos) {
        StringBuilder buf = new StringBuilder();

        buf.append(String.format("(%4x)  ", startPos));
        for (int i = 0; i < len; i++) {
            if (i == 4) {
                buf.append(" ");
            }
            buf.append(String.format("%2x ", bytes[i]));
        }

        for (int i = 0; i < len; i++) {
            if (i == 4) {
                buf.append(" ");
            }
            char ch = formatByte(bytes[i]);
            buf.append(ch);
        }
        Log.i("ClassRenamerTest", buf.toString());
    }

    private char formatByte(byte b) {
        int codePoint = new Byte(b).intValue();
        try {
            char[] chars = Character.toChars(codePoint);
            if (Character.isWhitespace(chars[0]) ||
                Character.isISOControl(chars[0])) {
                return '.';
            }
            return chars[0];
        } catch (IllegalArgumentException e) {
            return '.';
        }
    }
}
