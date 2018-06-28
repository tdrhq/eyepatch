package com.tdrhq.eyepatch.util;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class HexDump {
    public static void hexDump(File input) throws IOException {
        FileInputStream is = new FileInputStream(input);
        byte[] bytes = new byte[8];

        int len = 0;
        int pos = 0;
        while ((len = is.read(bytes)) > 0) {
            hexDumpPrintLine(bytes, len, pos);
            pos += len;
        }
    }

    private static void hexDumpPrintLine(byte[] bytes, int len, int startPos) {
        StringBuilder buf = new StringBuilder();

        buf.append(String.format("(%4x)  ", startPos));
        for (int i = 0; i < len; i++) {
            if (i == 4) {
                buf.append(" ");
            }
            buf.append(String.format("%2x ", bytes[i]));
        }

        for (int i = 0; i < len; i++) {
            if (i == 4) {
                buf.append(" ");
            }
            char ch = formatByte(bytes[i]);
            buf.append(ch);
        }
        Log.i("ClassRenamerTest", buf.toString());
    }

    private static char formatByte(byte b) {
        int codePoint = new Byte(b).intValue();
        try {
            char[] chars = Character.toChars(codePoint);
            if (Character.isWhitespace(chars[0]) ||
                Character.isISOControl(chars[0])) {
                return '.';
            }
            return chars[0];
        } catch (IllegalArgumentException e) {
            return '.';
        }
    }
}
