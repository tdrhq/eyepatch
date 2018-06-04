// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch;

import android.os.Bundle;
import android.support.test.runner.AndroidJUnitRunner;
import com.tdrhq.eyepatch.runner.DeviceValidator;

public class TestRunner extends AndroidJUnitRunner {
    @Override
    public void onCreate(Bundle args) {
        DeviceValidator.disableValidation = true;
        super.onCreate(args);
    }
}
