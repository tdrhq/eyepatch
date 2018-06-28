package com.tdrhq.eyepatch.renamer;

import com.android.dx.dex.file.ItemType;

class MapItem extends Streamable {
    @F(idx=1) short type;
    @F(idx=2) short unused;
    @F(idx=3) int size;
    @F(idx=4) @Offset int offset;

    public MapItem(DexFileReader dexFileReader) {
        super(dexFileReader);
    }

    public ItemType getItemType() {
        for (ItemType _type : ItemType.values()) {
            if (_type.getMapValue() == type) {
                return _type;
            }
        }
        throw new RuntimeException("unknown type");
    }
}
