package com.tdrhq.eyepatch.renamer;

class EncodedCatchHandlerList extends Streamable {
    @F(idx=1, uleb=true) int size;
    @F(idx=2, sizeIdx=1) EncodedCatchHandler[] list;

    public EncodedCatchHandlerList(DexFileReader dexFileReader) {
        super(dexFileReader);
    }
}
