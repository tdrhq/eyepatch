package com.tdrhq.eyepatch.renamer;

import com.android.dex.util.ByteInput;

import java.io.IOException;
import java.io.RandomAccessFile;

class MyByteInput implements ByteInput {
    RandomAccessFile raf;
    public MyByteInput(RandomAccessFile raf) {
        this.raf = raf;
    }

    @Override
    public byte readByte() {
        try {
            return raf.readByte();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
