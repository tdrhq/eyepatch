// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import com.google.common.collect.Lists;
import com.tdrhq.eyepatch.util.DexFileUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.immutable.ImmutableClassDef;
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
                public Rewriter<ClassDef> getClassDefRewriter(Rewriters rewriters) {
                    return new MyRewriter(real);
                }
            });
        DexFile rewrittenDexFile = rewriter.rewriteDexFile(template);

        return rewrittenDexFile;
    }

    static class MyRewriter implements Rewriter<ClassDef> {
        private DexFile real;

        public MyRewriter(DexFile real) {
            this.real = real;
        }

        @Override
        public ClassDef rewrite(ClassDef template) {

            List<Method> methods = Lists.newArrayList(template.getMethods());
            return new ImmutableClassDef(
                    template.getType(),
                    template.getAccessFlags(),
                    template.getSuperclass(),
                    template.getInterfaces(),
                    template.getSourceFile(),
                    template.getAnnotations(),
                    template.getFields(),
                    methods);
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
