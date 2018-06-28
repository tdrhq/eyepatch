package com.tdrhq.eyepatch.renamer;

class ProtoIdItem extends Streamable {
    @F(idx=1) int shortyIdx;
    @F(idx=2) int returnTypeIdx;
    @F(idx=3) @Offset int parametersOff;

    public ProtoIdItem(DexFileReader dexFileReader) {
        super(dexFileReader);
    }
}
