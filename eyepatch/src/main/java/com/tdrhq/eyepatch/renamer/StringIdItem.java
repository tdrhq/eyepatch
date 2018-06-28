package com.tdrhq.eyepatch.renamer;

class StringIdItem extends Streamable {
    @F(idx=1) @Offset int stringDataOff;
    int originalIndex = -1;

    public StringIdItem(DexFileReader reader) {
        super(reader);
    }
}
