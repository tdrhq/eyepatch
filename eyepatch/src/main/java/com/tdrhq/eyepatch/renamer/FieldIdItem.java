package com.tdrhq.eyepatch.renamer;

class FieldIdItem extends Streamable {
    @F(idx=1) short classIdx;
    @F(idx=2) short typeIdx;
    @F(idx=3) int nameIdx;

    public FieldIdItem(DexFileReader dexFileReader) {
        super(dexFileReader);
    }
}
