package com.tdrhq.eyepatch.renamer;

import com.android.dex.util.ByteOutput;

import java.io.IOException;
import java.io.RandomAccessFile;

class MyByteOutput implements ByteOutput {
    RandomAccessFile raf;
    public MyByteOutput(RandomAccessFile raf) {
        this.raf = raf;
    }

    @Override
    public void writeByte(int out) {
        try {
            raf.write((byte) out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
