package com.tdrhq.eyepatch.renamer;

class EncodedTypeAddrPair extends Streamable {
    @F(idx=1, uleb=true) long typeIdx;
    @F(idx=1, uleb=true) long addr;

    public EncodedTypeAddrPair(DexFileReader dexFileReader) {
        super(dexFileReader);
    }
}
