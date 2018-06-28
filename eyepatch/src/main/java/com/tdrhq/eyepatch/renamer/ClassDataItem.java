package com.tdrhq.eyepatch.renamer;

class ClassDataItem extends Streamable {
    @F(idx=1, uleb=true) int staticFieldsSize;
    @F(idx=2, uleb=true) int instanceFieldsSize;
    @F(idx=3, uleb=true) int directMethodsSize;
    @F(idx=4, uleb=true) int virtualMethodsSize;

    @F(idx=5, sizeIdx=1) EncodedField[] staticFields;
    @F(idx=6, sizeIdx=2) EncodedField[] instanceFields;
    @F(idx=7, sizeIdx=3) EncodedMethod[] directMethods;
    @F(idx=8, sizeIdx=4) EncodedMethod[] virtualMethods;

    @Override
    public boolean isAligned() {
        return false;
    }

    public ClassDataItem(DexFileReader dexFileReader) {
        super(dexFileReader);
    }
}
