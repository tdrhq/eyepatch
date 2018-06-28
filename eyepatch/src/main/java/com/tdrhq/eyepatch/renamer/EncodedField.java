package com.tdrhq.eyepatch.renamer;

class EncodedField extends Streamable {
    @F(idx=1, uleb=true) int fieldIdxDiff;
    @F(idx=2, uleb=true) int accessFlags;

    @Override
    public boolean isAligned() {
        return false;
    }

    public EncodedField(DexFileReader dexFileReader) {
        super(dexFileReader);
    }
}
