// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.renamer;

import java.io.IOException;
import java.io.RandomAccessFile;
import com.android.dex.Leb128;

public class RafUtil {
    public static int readUInt(RandomAccessFile raf) throws IOException {
        int it = raf.readInt();
        it = Integer.reverseBytes(it);
        return it;
    }

    public static void writeUInt(RandomAccessFile raf, int it) throws IOException {
        it = Integer.reverseBytes(it);
        raf.writeInt(it);
    }

    public static short readUShort(RandomAccessFile raf) throws IOException {
        short it = raf.readShort();
        it = Short.reverseBytes(it);
        return it;
    }

    public static int readULeb128(RandomAccessFile raf) throws IOException {
        return Leb128.readUnsignedLeb128(new MyByteInput(raf));
    }

    public static int readSLeb128(RandomAccessFile raf) throws IOException {
        return Leb128.readSignedLeb128(new MyByteInput(raf));
    }

    public static short[] readShortArray(int size, RandomAccessFile raf) throws IOException {
        short[] ret = new short[size];
        for (int i = 0; i < size; i++) {
            ret[i] = RafUtil.readUShort(raf);
        }
        return ret;
    }
}
