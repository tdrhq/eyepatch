package com.tdrhq.eyepatch.renamer;

import java.io.IOException;
import java.io.RandomAccessFile;

class EncodedCatchHandler extends Streamable {
    long size;
    EncodedTypeAddrPair[] handlers;
    long catchAllAddr;

    public EncodedCatchHandler(DexFileReader dexFileReader) {
        super(dexFileReader);
    }

    @Override
    public void readImpl(RandomAccessFile raf) throws IOException {
        size = RafUtil.readSLeb128(raf);

        // handle negative numbers here.
        if (size != 0) {

            throw new RuntimeException("unexpected: " + size);
        }

        handlers = DexFileReader.readArray(Math.abs((int) size), EncodedTypeAddrPair.class, this, raf);
        catchAllAddr = RafUtil.readULeb128(raf);
    }

}
