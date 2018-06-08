// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.runner;

import android.os.Build;

public class DeviceValidator {
    private static final String SDK_INT_MESSAGE =
            "EyePatch is only supported on Jellybean or higher devices.\n";

    public static void assertDeviceIsAcceptable() {
        if (Build.VERSION.SDK_INT < 16) {
            throw new UnsupportedOperationException(
                    SDK_INT_MESSAGE);
        }

        validateDexoptDisabled();
    }

    private static void validateDexoptDisabled() {
        if (Build.VERSION.SDK_INT >= 21) {
            return;
        }

        if (!getDexOptFlag("v").equals("n") ||
            !getDexOptFlag("o").equals("n")) {
            throw new UnsupportedOperationException();
        }
    }

    private static String getDexOptFlag(String flagName) {
        String allFlags = System.getProperty("dalvik.vm.dexopt-flags");
        return parseDexOptFlags(allFlags, flagName);
    }

    static String parseDexOptFlags(String allFlags, String flagName) {
        if (allFlags == null) {
            return "";
        }

        String[] parts = allFlags.split(",");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            String[] keyValue = part.split("=");
            if (keyValue[0].equals(flagName)) {
                if (keyValue.length == 1) {
                    return "";
                }
                return keyValue[1];
            }
        }

        return "";
    }
}
