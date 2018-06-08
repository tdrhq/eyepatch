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
    }
}
