// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import com.google.common.collect.Lists;
import com.tdrhq.eyepatch.util.DexFileUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction21s;
import org.jf.dexlib2.rewriter.DexRewriter;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.RewriterModule;
import org.jf.dexlib2.rewriter.Rewriters;
import org.jf.dexlib2.writer.io.FileDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

/**
 * Merges a template implementation with a real implementation.
 *
 * This allows the template implementation to dispatch to the
 * Dispatcher, but if the Dispatcher responds by saying that we need
 * to run the original code, then this bypasses to the original
 * implementation.
 */
public class Merger {
    public Merger() {
    }

    public void mergeDexFile(File template, File realCode, File output) throws IOException {
        FileInputStream is = new FileInputStream(template);
        FileInputStream realCodeStream = new FileInputStream(realCode);

        mergeDexFile(is, realCodeStream, output);

        is.close();
        realCodeStream.close();
    }

    public void mergeDexFile(InputStream template, InputStream realCode, File output) throws IOException {
        int length;

        DexBackedDexFile templateDexFile = DexFileUtil.readDexFile(template);
        DexBackedDexFile realDexFile = DexFileUtil.readDexFile(realCode);

        DexFile merged = mergeDexFile(templateDexFile, realDexFile);

        FileDataStore dataStore = new FileDataStore(output);
        DexPool.writeTo(dataStore, merged);
        dataStore.close();
    }

    DexFile mergeDexFile(final DexFile template, final DexFile real) {
        DexRewriter rewriter = new DexRewriter(new RewriterModule() {
                @Override
                public Rewriter<Method> getMethodRewriter(Rewriters rewriters) {
                    return new MyRewriter(real);
                }
            });
        DexFile rewrittenDexFile = rewriter.rewriteDexFile(template);

        return rewrittenDexFile;
    }

    static Method mergeMethods(Method oldMethod, Method realMethod) {
        MethodImplementation impl = oldMethod.getImplementation();
        int params = registersForParameters(oldMethod);
        List<Instruction> instructions = Lists.newArrayList(impl.getInstructions());
        // get rid of the final instruction
        Instruction last = instructions.get(instructions.size() - 1);

        if (last.getOpcode() != Opcode.THROW) {
            throw new IllegalStateException("this doesn't look like a template now does it");
        }

        instructions.remove(instructions.size() - 1);

        for (Instruction ins : realMethod.getImplementation().getInstructions()) {
            instructions.add(ins);
        }

        impl = new ImmutableMethodImplementation(
                impl.getRegisterCount() +
                realMethod.getImplementation().getRegisterCount() - params,
                instructions,
                impl.getTryBlocks(),
                impl.getDebugItems());

        return new ImmutableMethod(
                oldMethod.getDefiningClass(),
                oldMethod.getName(),
                oldMethod.getParameters(),
                oldMethod.getReturnType(),
                oldMethod.getAccessFlags(),
                oldMethod.getAnnotations(),
                impl);
    }

    static int registersForParameters(Method method) {
        return method.getParameters().size() + 1;
    }

    static class MyRewriter implements Rewriter<Method> {
        private DexFile real;

        public MyRewriter(DexFile real) {
            this.real = real;
        }

        @Override
        public Method rewrite(Method oldMethod) {
            Method realMethod = findRealImpl(oldMethod);

            return oldMethod;
            //return mergeMethods(oldMethod, realMethod);
        }



        private int fixReg(int params, int templateReg, int reg) {
            if (reg < params) {
                return reg;
            }
            return templateReg + reg - params;
        }

        Method findRealImpl(Method oldMethod) {
            for (ClassDef realClass : real.getClasses()) {
                if (!realClass.getType().equals(oldMethod.getDefiningClass())) {
                    continue;
                }

                method_loop: for (Method realMethod : realClass.getMethods()) {
                    if (!realMethod.getName().equals(oldMethod.getName())) {
                        continue;
                    }

                    if (oldMethod.getParameters().size() != realMethod.getParameters().size()) {
                        continue;
                    }

                    for (int i = 0; i < oldMethod.getParameters().size(); i++) {
                        String oldType = oldMethod.getParameters().get(i).getType();
                        String realType= realMethod.getParameters().get(i).getType();
                        if (!oldType.equals(realType)) {
                            continue method_loop;
                        }
                    }

                    return realMethod;

                }
            }

            throw new RuntimeException("couldn't find real implementation");
        }
    }
}
