package com.tdrhq.eyepatch.renamer;

import android.util.Log;

import java.io.IOException;
import java.io.RandomAccessFile;

class EncodedMethod extends Streamable {
    @F(idx=1, uleb=true) int methodIdxDiff;
    @F(idx=2, uleb=true) int accessFlags;
    @F(idx=3, uleb=true) @Offset int codeOff;

    @Override
    public boolean isAligned() {
        return false;
    }

    public EncodedMethod(DexFileReader dexFileReader) {
        super(dexFileReader);
    }

    @Override
    public void readImpl(RandomAccessFile raf) throws IOException {
        super.readImpl(raf);
        Log.i("DexFileReader", "Read encoded method as: " + methodIdxDiff + " " + accessFlags + " " + codeOff);
    }
}
