package com.tdrhq.eyepatch.renamer;

import java.io.IOException;
import java.io.RandomAccessFile;

class DebugInfoItem extends Streamable {
    int lineStart;
    int parametersSize;
    int[] parameterNames;

    public DebugInfoItem(DexFileReader dexFileReader) {
        super(dexFileReader);
    }

    @Override
    public boolean isAligned() {
        return false;
    }

    @Override
    public void readImpl(RandomAccessFile raf) throws IOException {
        lineStart = RafUtil.readULeb128(raf);
        parametersSize = RafUtil.readULeb128(raf);
        parameterNames = new int[parametersSize];
        for (int i = 0; i < parametersSize; i++) {
            parameterNames[i] = RafUtil.readULeb128(raf);
        }
    }
}
