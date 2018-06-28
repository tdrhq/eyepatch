package com.tdrhq.eyepatch.renamer;

import java.io.IOException;

class TypeIdItem extends Streamable {
    @F(idx=1) int descriptorIdx; // a  pointer to string_ids

    public TypeIdItem(DexFileReader dexFileReader) {
        super(dexFileReader);
    }

    public String getString() throws IOException {
        return dexFileReader.getString(descriptorIdx);
    }
}
