package com.tdrhq.eyepatch.renamer;

class TryItem extends Streamable {
    @F(idx=1) int startAddr;
    @F(idx=2) short insnCount;
    @F(idx=3) @Offset short handlerOff;
    public TryItem(DexFileReader dexFileReader) {
        super(dexFileReader);
    }
}
