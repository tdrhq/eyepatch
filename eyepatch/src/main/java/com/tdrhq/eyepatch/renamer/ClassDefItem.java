package com.tdrhq.eyepatch.renamer;

import java.io.IOException;
import java.io.RandomAccessFile;

class ClassDefItem extends Streamable {
    @F(idx=1) int classIdx;
    @F(idx=2) int accessFlags;
    @F(idx=3) int superclassIdx;
    @F(idx=4) @Offset int interfacesOff;
    @F(idx=5) int sourceFileIdx;
    @F(idx=6) @Offset int annotationsOff;
    @F(idx=7) @Offset int classDataOff;
    @F(idx=8) @Offset int staticValuesOff;

    public ClassDefItem(DexFileReader dexFileReader) {
        super(dexFileReader);
    }


    @Override
    public void readImpl(RandomAccessFile raf) throws IOException {
        readObject(raf);
    }

}
