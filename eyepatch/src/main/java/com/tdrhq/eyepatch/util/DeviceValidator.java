// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.tdrhq.eyepatch.util.SystemProperties;

public class DeviceValidator {
    private static final String SDK_INT_MESSAGE =
            "EyePatch is only supported on Jellybean or higher devices.\n";

    private static final String DEXOPT_FLAG = "dalvik.vm.dexopt-flags";

    private static final String DISABLE_DEXOPT_MESSAGE =
            "DexOpt needs to be disabled when running on KitKat or lower. " +
            "YOu can do this by running: \n" +
            "  $ adb shell setprop " + DEXOPT_FLAG + " v=n,o=n";
    private static String VM_SAFE_MODE_MESSAGE =
            "vmSafeMode needs to be enabled. In your manifest <application> tag add vmSafeMode=\"true\" attribute";

    public static void assertDeviceIsAcceptable(Context context) {
        if (Build.VERSION.SDK_INT < 16) {
            throw new UnsupportedOperationException(
                    SDK_INT_MESSAGE);
        }


        if (Build.VERSION.SDK_INT < 21) {
            validateDexoptDisabled();
        }

        validateVmSafeMode(context);
    }

    private static void validateVmSafeMode(Context context) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(),
                    0
            );
            if ((appInfo.flags & ApplicationInfo.FLAG_VM_SAFE_MODE) == 0) {
                throw new UnsupportedOperationException(VM_SAFE_MODE_MESSAGE);
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void validateDexoptDisabled() {
        if (!getDexOptFlag("v").equals("n") ||
            !getDexOptFlag("o").equals("n")) {
            throw new UnsupportedOperationException(DISABLE_DEXOPT_MESSAGE);
        }
    }

    private static String getDexOptFlag(String flagName) {
        String allFlags = SystemProperties.getSystemProperty(DEXOPT_FLAG);
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
