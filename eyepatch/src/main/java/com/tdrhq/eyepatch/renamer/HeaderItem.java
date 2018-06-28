package com.tdrhq.eyepatch.renamer;

import android.util.Log;

import java.io.IOException;
import java.io.RandomAccessFile;

class HeaderItem extends Streamable {
    @F(idx=1) byte[] magic = new byte[8];
    @F(idx=2) int checksum;
    @F(idx=3) byte[] signature = new byte[20];
    @F(idx=4) int fileSize;
    @F(idx=5) int headerSize;
    @F(idx=6) int endianTag;
    @F(idx=7) int linkSize;
    @F(idx=8) @Offset int linkOff;
    @F(idx=9) @Offset int mapOff;
    @F(idx=10) int stringIdsSize;
    @F(idx=11) @Offset int stringIdsOff;
    @F(idx=12) int typeIdsSize;
    @F(idx=13) @Offset int typeIdsOff;
    @F(idx=14) int protoIdsSize;
    @F(idx=15) @Offset int protoIdsOff;
    @F(idx=16) int fieldIdsSize;
    @F(idx=17) @Offset int fieldIdsOff;
    @F(idx=18) int methodIdsSize;
    @F(idx=19) @Offset int methodIdsOff;
    @F(idx=20) int classDefsSize;
    @F(idx=21) @Offset int classDefsOff;
    @F(idx=22) int dataSize;
    @F(idx=23) @Offset int dataOff;

    public HeaderItem(DexFileReader dexFileReader) {
        super(dexFileReader);
    }

    @Override
    public void readImpl(RandomAccessFile raf) throws IOException {
        super.readImpl(raf);
        Log.i("DexFileReader", "checksum is: " + checksum + " " + String.format("%8x", checksum));
    }
}
