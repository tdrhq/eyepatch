package com.tdrhq.eyepatch.renamer;

class MethodIdItem extends Streamable {
    @F(idx=1) short classIdx;
    @F(idx=2) short protoIdx;
    @F(idx=3) int nameIdx;

    public MethodIdItem(DexFileReader dexFileReader) {
        super(dexFileReader);
    }
}
