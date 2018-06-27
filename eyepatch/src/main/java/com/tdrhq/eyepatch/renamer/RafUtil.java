// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.renamer;

import java.io.IOException;
import java.io.RandomAccessFile;

public class RafUtil {
    public static int readUInt(RandomAccessFile raf) throws IOException {
        int it = raf.readInt();
        it = Integer.reverseBytes(it);
        return it;
    }
}
