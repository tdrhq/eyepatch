package com.tdrhq.eyepatch.renamer;

class MapList extends Streamable {
    @F(idx=1) int size;
    @F(idx=2, sizeIdx=1) MapItem[] list;

    public MapList(DexFileReader dexFileReader) {
        super(dexFileReader);
    }
}
