package com.tdrhq.eyepatch.renamer;

import java.io.IOException;
import java.io.RandomAccessFile;

class CodeItem extends Streamable {
    @F(idx=1) short registersSize;
    @F(idx=2) short insSize;
    @F(idx=3) short outsSize;
    @F(idx=4) short triesSize;
    @F(idx=5) @Offset int debugInfoOff;
    @F(idx=6) int insnsSize;
    @F(idx=7, sizeIdx=6) short[] insns;

    // these are conditional
    short padding = 0;
    DexFileReader.TryItem[] tryItems;
    DexFileReader.EncodedCatchHandlerList encodedCatchHandlerList = null;

    public CodeItem(DexFileReader dexFileReader) {
        super(dexFileReader);
    }

    @Override
    public void readImpl(RandomAccessFile raf) throws IOException {
        readObject(raf);

        if (insnsSize % 2 != 0 && triesSize > 0) {
            padding = RafUtil.readUShort(raf);
        }

        if (triesSize > 0) {
            tryItems = DexFileReader.readArray(triesSize, DexFileReader.TryItem.class, dexFileReader, raf);
            encodedCatchHandlerList = new DexFileReader.EncodedCatchHandlerList(dexFileReader);
            encodedCatchHandlerList.read(raf);
        }
    }

    @Override
    public void writeImpl(RandomAccessFile raf) throws IOException {
        writeObject(raf);

        if (insnsSize % 2 != 0) {

            // acc. to the doc this should only be written if
            // triesSize >0, but it should be safe to always write
            // it, and dexmaker seems to be doing that too
            RafUtil.writeUShort(raf, padding);
        }

        if (triesSize > 0) {
            DexFileReader.writeArray(tryItems, raf);
            encodedCatchHandlerList.write(raf);
        }
    }
}
