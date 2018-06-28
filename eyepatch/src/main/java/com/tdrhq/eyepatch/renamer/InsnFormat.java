// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.renamer;

import java.util.HashMap;
import java.util.Map;

public class InsnFormat {
    // This list was parsed out from https://source.android.com/devices/tech/dalvik/instruction-formats
    static String format =
            "00 10x     \n" +
            "01 12x     \n" +
            "02 22x     \n" +
            "03 32x     \n" +
            "04 12x     \n" +
            "05 22x     \n" +
            "06 32x     \n" +
            "07 12x     \n" +
            "08 22x     \n" +
            "09 32x     \n" +
            "0a 11x     \n" +
            "0b 11x     \n" +
            "0c 11x     \n" +
            "0d 11x     \n" +
            "0e 10x     \n" +
            "0f 11x     \n" +
            "10 11x     \n" +
            "11 11x     \n" +
            "12 11n     \n" +
            "13 21s     \n" +
            "14 31i     \n" +
            "15 21h     \n" +
            "16 21s     \n" +
            "17 31i     \n" +
            "18 51l     \n" +
            "19 21h     \n" +
            "1a 21c     \n" +
            "1b 31c     \n" +
            "1c 21c     \n" +
            "1d 11x     \n" +
            "1e 11x     \n" +
            "1f 21c     \n" +
            "20 22c     \n" +
            "21 12x     \n" +
            "22 21c     \n" +
            "23 22c     \n" +
            "24 35c     \n" +
            "25 3rc     \n" +
            "26 31t     \n" +
            "27 11x     \n" +
            "28 10t     \n" +
            "29 20t     \n" +
            "2a 30t     \n" +
            "2b 31t     \n" +
            "2c 31t     \n" +
            "2d-31 23x \n" +
            "32-37 22t \n" +
            "38-3d 21t \n" +
            "3e-43 10x \n" +
            "44-51 23x \n" +
            "52-5f 22c \n" +
            "60-6d 21c \n" +
            "6e-72 35c \n" +
            "73 10x     \n" +
            "74-78 3rc \n" +
            "79-7a 10x \n" +
            "7b-8f 12x \n" +
            "90-af 23x \n" +
            "b0-cf 12x \n" +
            "d0-d7 22s \n" +
            "d8-e2 22b \n" +
            "e3-f9 10x \n" +
            "fa 45cc    \n" +
            "fb 4rcc    \n" +
            "fc 35c     \n" +
            "fd 3rc     \n" +
            "fe 21c     \n" +
            "ff 21c     \n";

    static Map<Integer, Integer> mapping = new HashMap<>();

    static {
        String[] parts = format.split("\\s+");
        for (int i = 0; i < parts.length; i+=2) {
            String key = parts[i];
            String val = parts[i+1];

            String startS;
            String endS;

            if (key.contains("-")) {
                String[] pair = key.split("-");
                startS = pair[0];
                endS = pair[1];
            } else {
                startS = key;
                endS = key;
            }

            int start = Integer.parseInt(startS, 16);
            int end = Integer.parseInt(endS, 16);

            int len = Integer.parseInt(val.substring(0, 1));
            for (int j = start; j <= end; j++) {
                mapping.put(j, len);
            }
        }
    }

    public static int getLength(int insn) {
        return mapping.get(insn);
    }
}
