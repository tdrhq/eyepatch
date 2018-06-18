// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.renamer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Provides contents for a dex file.
 *
 * While it's nice to be able to generate it on the fly for tests,
 * DexFileReader in particular would have nicer tests if we could
 * assert on exact outputs.
 */
public class StaticDexProvider {

    /**
     * I know.. this looks weird, but makes it easier to debug the
     * tests. We parse this to generate bytes later.
     */
    static String data =
            /* (   0) */ "64 65 78  a  30 33 35  0 " +
            /* (   8) */ "d2 39 78 33  6b c2 9d f6 " +
            /* (  10) */ "af  5 4f f1  2d fc af b6 " +
            /* (  18) */ "7a 70 15  6  fd ce 72 a2 " +
            /* (  20) */ "c4  1  0  0  70  0  0  0 " +
            /* (  28) */ "78 56 34 12   0  0  0  0 " +
            /* (  30) */ " 0  0  0  0  48  1  0  0 " +
            /* (  38) */ " 7  0  0  0  70  0  0  0 " +
            /* (  40) */ " 3  0  0  0  8c  0  0  0 " +
            /* (  48) */ " 1  0  0  0  98  0  0  0 " +
            /* (  50) */ " 0  0  0  0   0  0  0  0 " +
            /* (  58) */ " 1  0  0  0  a4  0  0  0 " +
            /* (  60) */ " 1  0  0  0  ac  0  0  0 " +
            /* (  68) */ "f8  0  0  0  cc  0  0  0 " +
            /* (  70) */ "e4  0  0  0  f3  0  0  0 " +
            /* (  78) */ "f6  0  0  0   5  1  0  0 " +
            /* (  80) */ "19  1  0  0  2d  1  0  0 " +
            /* (  88) */ "35  1  0  0   2  0  0  0 " +
            /* (  90) */ " 3  0  0  0   4  0  0  0 " +
            /* (  98) */ " 1  0  0  0   2  0  0  0 " +
            /* (  a0) */ " 0  0  0  0   0  0  0  0 " +
            /* (  a8) */ " 5  0  0  0   0  0  0  0 " +
            /* (  b0) */ " 1  0  0  0   1  0  0  0 " +
            /* (  b8) */ " 0  0  0  0   0  0  0  0 " +
            /* (  c0) */ " 0  0  0  0  3f  1  0  0 " +
            /* (  c8) */ " 0  0  0  0   1  0  0  0 " +
            /* (  d0) */ " 0  0  0  0   0  0  0  0 " +
            /* (  d8) */ " 3  0  0  0  1a  0  6  0 " +
            /* (  e0) */ "11  0  0  0   d 46 6f 6f " +
            /* (  e8) */ "2e 67 65 6e  65 72 61 74 " +
            /* (  f0) */ "65 64  0  1  4c  0  d 4c " +
            /* (  f8) */ "63 6f 6d 2f  66 6f 6f 2f " +
            /* ( 100) */ "46 6f 6f 3b   0 12 4c 6a " +
            /* ( 108) */ "61 76 61 2f  6c 61 6e 67 " +
            /* ( 110) */ "2f 4f 62 6a  65 63 74 3b " +
            /* ( 118) */ " 0 12 4c 6a  61 76 61 2f " +
            /* ( 120) */ "6c 61 6e 67  2f 53 74 72 " +
            /* ( 128) */ "69 6e 67 3b   0  6 67 65 " +
            /* ( 130) */ "74 42 61 72   0  8 7a 6f " +
            /* ( 138) */ "69 64 62 65  72 67  0  0 " +
            /* ( 140) */ " 0  1  0  0   9 cc  1  0 " +
            /* ( 148) */ " a  0  0  0   0  0  0  0 " +
            /* ( 150) */ " 1  0  0  0   0  0  0  0 " +
            /* ( 158) */ " 1  0  0  0   7  0  0  0 " +
            /* ( 160) */ "70  0  0  0   2  0  0  0 " +
            /* ( 168) */ " 3  0  0  0  8c  0  0  0 " +
            /* ( 170) */ " 3  0  0  0   1  0  0  0 " +
            /* ( 178) */ "98  0  0  0   5  0  0  0 " +
            /* ( 180) */ " 1  0  0  0  a4  0  0  0 " +
            /* ( 188) */ " 6  0  0  0   1  0  0  0 " +
            /* ( 190) */ "ac  0  0  0   1 20  0  0 " +
            /* ( 198) */ " 1  0  0  0  cc  0  0  0 " +
            /* ( 1a0) */ " 2 20  0  0   7  0  0  0 " +
            /* ( 1a8) */ "e4  0  0  0   0 20  0  0 " +
            /* ( 1b0) */ " 1  0  0  0  3f  1  0  0 " +
            /* ( 1b8) */ " 0 10  0  0   1  0  0  0 " +
            /* ( 1c0) */ "48  1  0  0";

    public StaticDexProvider() {
    }

    public static byte[] getBytes() {
        String[] splitData = data.split(" ");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        for (String b : splitData) {
            if (b.equals("")) {
                continue;
            }
            os.write((byte) (Long.parseLong(b.toUpperCase(), 16) & 0xFF));
        }
        return os.toByteArray();
    }

    public static void writeToFile(File file) throws IOException {
        FileOutputStream os = new FileOutputStream(file);
        byte[] data = getBytes();
        os.write(data);
        os.close();
    }
}
