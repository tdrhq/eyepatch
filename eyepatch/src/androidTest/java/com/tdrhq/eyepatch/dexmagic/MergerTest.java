// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import android.util.Log;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.tdrhq.eyepatch.EyePatchTemporaryFolder;
import com.tdrhq.eyepatch.util.ClassLoaderIntrospector;
import com.tdrhq.eyepatch.util.DexFileUtil;
import com.tdrhq.eyepatch.util.Util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.ExceptionHandler;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.TryBlock;
import org.jf.dexlib2.iface.debug.DebugItem;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.ImmutableAnnotation;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableDexFile;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.ImmutableMethodParameter;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction11x;
import org.jf.dexlib2.writer.io.FileDataStore;
import org.jf.dexlib2.writer.pool.DexPool;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class MergerTest {
    @Rule
    public EyePatchTemporaryFolder tmpdir = new EyePatchTemporaryFolder();

    Method templateMethod;
    ClassDef template;

    Method realMethod;
    ClassDef realClass;

    @Before
    public void before() throws Throwable {

        MethodImplementation methodImplementation = new ImmutableMethodImplementation(
                10,
                Lists.newArrayList(
                        new ImmutableInstruction11x(Opcode.THROW, 0)),
                new ArrayList<TryBlock<? extends ExceptionHandler>>(),
                new ArrayList<DebugItem>());

        templateMethod  = new ImmutableMethod(
                "Lcom/foo/Foo;",
                "bar",
                new ArrayList<ImmutableMethodParameter>(),
                "V",
                Modifier.PUBLIC,
                ImmutableSet.copyOf(new ArrayList<ImmutableAnnotation>()),
                methodImplementation);

        List<Method> methods = new ArrayList<Method>();
        methods.add(templateMethod);
        template = new ImmutableClassDef(
                "Lcom/foo/Foo;",
                Modifier.PUBLIC,
                "Ljava/lang/Object;",
                new ArrayList<String>(),
                "Foo.generated",
                new ArrayList<Annotation>(),
                new ArrayList<Field>(),
                methods);

        methodImplementation = new ImmutableMethodImplementation(
                10,
                new ArrayList<Instruction>(),
                new ArrayList<TryBlock<? extends ExceptionHandler>>(),
                new ArrayList<DebugItem>());

        realMethod  = new ImmutableMethod(
                "Lcom/foo/Foo;",
                "bar",
                new ArrayList<ImmutableMethodParameter>(),
                "V",
                Modifier.PUBLIC,
                ImmutableSet.copyOf(new ArrayList<ImmutableAnnotation>()),
                methodImplementation);

        methods.add(templateMethod);
        realClass = new ImmutableClassDef(
                "Lcom/foo/Foo;",
                Modifier.PUBLIC,
                "Ljava/lang/Object;",
                new ArrayList<String>(),
                "Foo.generated",
                new ArrayList<Annotation>(),
                new ArrayList<Field>(),
                methods);
    }


    /**
     * Creates a new DexFile with just the given class extracted out.
     */
    private File extractClass(Class klass)  throws IOException {
        File ret = ClassLoaderIntrospector.getDefiningDexFile(klass);
        DexBackedDexFile dexfile = DexFileUtil.readDexFile(ret);
        Log.i("MergerTest", "Finished reading the DexBackedDexFile");

        File tmpOutput = tmpdir.newFile("tmpoutput.dex");
        ClassDef theClassDef = DexFileUtil.findClassDef(dexfile, klass);

        assertNotNull(theClassDef);
        DexFile copy = new ImmutableDexFile(
                Opcodes.forApi(16),
                ImmutableSet.of(theClassDef));
        FileDataStore dataStore = new FileDataStore(tmpOutput);
        DexPool.writeTo(dataStore, copy);
        dataStore.close();
        return tmpOutput;
    }

    @Test
    public void testPreconditions() throws Throwable {
        File file = extractClass(Foo.class);
        assertNotNull(file);
        assertThat(Collections.list(Util.loadDexFile(file).entries()),
                   hasItem(Foo.class.getName()));
    }

    @Test
    public void testMerge() throws Throwable {
        DexFile merged = new Merger()
                .mergeDexFile(
                        new ImmutableDexFile(Opcodes.forApi(14), ImmutableSet.of(template)),
                        new ImmutableDexFile(Opcodes.forApi(14), ImmutableSet.of(realClass)));

        assertEquals(1, merged.getClasses().size());
        List<Method> methods = Lists.newArrayList(merged.getClasses().iterator().next().getMethods());

        assertEquals(2, methods.size());
    }

    static class Foo {
    }
}
