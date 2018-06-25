// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.renamer;

import com.android.dex.Leb128;
import com.android.dex.util.ByteInput;
import com.android.dx.dex.DexOptions;
import com.android.dx.dex.file.DexFile;
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

    public DexFile read() throws IOException {

        DexFile dexFile = new DexFile(new DexOptions());
        RandomAccessFile raf = new RandomAccessFile(file, "r");

        headerItem = new HeaderItem();
        headerItem.read(raf);

        raf.seek(headerItem.stringIdsOff);
        stringIdItems = new StringIdItem[(int) headerItem.stringIdsSize];
        for (int i = 0; i < headerItem.stringIdsSize; i ++) {
            stringIdItems[i] = new StringIdItem();
            stringIdItems[i].read(raf);
        }

        return dexFile;
    }

    class HeaderItem {
        long stringIdsSize;
        long stringIdsOff;

        public void read(RandomAccessFile raf) throws IOException {
            raf.seek(0);
            raf.skipBytes(8); // magic
            raf.readInt();    // checksum
            raf.skipBytes(20); // signature
            for (int i = 0; i < 6; i++) {
                raf.readInt(); // bunch of stuff
            }
            stringIdsSize = readUInt(raf);
            stringIdsOff = readUInt(raf);
        }
    }

    class StringIdItem {
        long stringDataOff;

        public void read(RandomAccessFile raf) throws IOException {
            stringDataOff = readUInt(raf);
        }
    }

    static long readUInt(RandomAccessFile raf) throws IOException {
        int it = raf.readInt();
        it = Integer.reverseBytes(it);
        long ret = (long) it;
        return ret;
    }

    static long readULeb128(RandomAccessFile raf) throws IOException {
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
