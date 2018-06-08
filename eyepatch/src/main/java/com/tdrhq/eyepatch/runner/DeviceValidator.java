// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.runner;

import android.os.Build;

public class DeviceValidator {
    public static boolean disableValidation = false;
    private static final String SDK_INT_MESSAGE =
            "EyePatch is only supported on Jellybean or higher devices.\n";

    public static void assertDeviceIsAcceptable() {
        if (isEyePatchTest()) {
            return;
        }

        if (Build.VERSION.SDK_INT < 16) {
            throw new UnsupportedOperationException(
                    SDK_INT_MESSAGE);
        }
    }

    /**
     * Even though we're not publicly supporting < Lollipop,
     * internally we're still working on supporting it, so we want all
     * our tests to be able to run on thse devices. So if we're
     * running under an eyepatch test we're going to disable these
     * checks.
     */
    private static boolean isEyePatchTest() {
        return disableValidation;
    }
}
