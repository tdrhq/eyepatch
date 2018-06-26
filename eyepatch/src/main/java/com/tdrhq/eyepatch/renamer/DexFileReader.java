// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.renamer;

import com.android.dex.Leb128;
import com.android.dex.Mutf8;
import com.android.dex.util.ByteInput;
import com.android.dx.dex.DexOptions;
import com.android.dx.dex.file.ClassDefItem;
import com.android.dx.dex.file.DexFile;
import com.android.dx.rop.cst.CstString;
import com.android.dx.rop.cst.CstType;
import com.android.dx.rop.type.StdTypeList;
import com.android.dx.rop.type.Type;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Reads a {@code DexFile} from the given file.
 *
 * This lets us modifiy the structures of the DexFile in order to
 * rewrite it as something else.
 */
public class DexFileReader {
    private File file;
    public DexFileReader(File file) {
        this.file = file;
    }

    HeaderItem headerItem = null;
    StringIdItem[] stringIdItems = null;
    RandomAccessFile raf;
    _ClassDefItem[] classDefItems = null;

    public DexFile read() throws IOException {

        DexFile dexFile = new DexFile(new DexOptions());
        raf = new RandomAccessFile(file, "r");
        headerItem = new HeaderItem();
        headerItem.read();

        raf.seek(headerItem.stringIdsOff);
        stringIdItems = new StringIdItem[(int) headerItem.stringIdsSize];
        for (int i = 0; i < headerItem.stringIdsSize; i ++) {
            stringIdItems[i] = new StringIdItem();
            stringIdItems[i].read();
        }

        raf.seek(headerItem.classDefsOff);
        classDefItems = new _ClassDefItem[(int) headerItem.classDefsSize];
        for (int i = 0; i < headerItem.classDefsSize; i++) {
            classDefItems[i] = new _ClassDefItem();
            classDefItems[i].read();
        }

        return dexFile;
    }

    String getString(long idx) throws IOException {
        return stringIdItems[(int) idx].getString();
    }

    class HeaderItem {
        long stringIdsSize;
        long stringIdsOff;
        long classDefsSize;
        long classDefsOff;

        public void read() throws IOException {
            raf.seek(0);
            raf.skipBytes(8); // magic
            raf.readInt();    // checksum
            raf.skipBytes(20); // signature
            for (int i = 0; i < 6; i++) {
                raf.readInt(); // bunch of stuff
            }
            stringIdsSize = readUInt();
            stringIdsOff = readUInt();

            for (int i = 0; i < 8; i++) {
                readUInt();
            }

            classDefsSize = readUInt();
            classDefsOff = readUInt();
        }
    }

    class _ClassDefItem {
        long classIdx;
        long accessFlags;
        long superclassIdx;
        long interfacesOff;
        long sourceFileIdx;
        long annotationsOff;
        long classDataOff;
        long staticValuesOff;

        public void read() throws IOException {
            classIdx = readUInt();
            accessFlags = readUInt();
            superclassIdx = readUInt();
            interfacesOff = readUInt();
            sourceFileIdx = readUInt();
            annotationsOff = readUInt();
            classDataOff = readUInt();
            staticValuesOff = readUInt();
        }

        public ClassDefItem toClassDefItem() throws IOException {
            return new ClassDefItem(
                    new CstType(Type.intern(getString(classIdx) + "_suffix")),
                    (int) accessFlags,
                    new CstType(Type.intern(getString(superclassIdx))),
                    StdTypeList.EMPTY, // TODO: fill list
                    new CstString(getString(sourceFileIdx)));
        }
    }

    class StringIdItem {
        long stringDataOff;

        public void read() throws IOException {
            stringDataOff = readUInt();
        }

        public String getString() throws IOException {
            raf.seek(stringDataOff);
            int len = readULeb128();
            char[] data = new char[len];
            return Mutf8.decode(new MyByteInput(raf), data);
        }
    }

    long readUInt() throws IOException {
        int it = raf.readInt();
        it = Integer.reverseBytes(it);
        long ret = (long) it;
        return ret;
    }

    int readULeb128() throws IOException {
        return Leb128.readUnsignedLeb128(new MyByteInput(raf));
    }

    static class MyByteInput implements ByteInput {
        RandomAccessFile raf;
        public MyByteInput(RandomAccessFile raf) {
            this.raf = raf;
        }

        @Override
        public byte readByte() {
            try {
                return raf.readByte();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
