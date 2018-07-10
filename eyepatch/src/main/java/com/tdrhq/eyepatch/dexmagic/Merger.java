// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import com.tdrhq.eyepatch.util.DexFileUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MethodImplementation;
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

    private DexFile mergeDexFile(final DexFile template, final DexFile real) {
        DexRewriter rewriter = new DexRewriter(new RewriterModule() {
                public Rewriter<MethodImplementation> getMethodImplementationRewriter(Rewriters rewriters) {
                    return new MergedMethodImplementationRewriter(template, real);
                }
            });
        DexFile rewrittenDexFile = rewriter.rewriteDexFile(template);

        return rewrittenDexFile;
    }

    static class MergedMethodImplementationRewriter implements Rewriter<MethodImplementation> {
        private DexFile template;
        private DexFile real;

        public MergedMethodImplementationRewriter(DexFile template, DexFile real) {
            this.template = template;
            this.real = real;
        }

        @Override
        public MethodImplementation rewrite(MethodImplementation methodImplementation) {
            return methodImplementation;
        }
    }
}
