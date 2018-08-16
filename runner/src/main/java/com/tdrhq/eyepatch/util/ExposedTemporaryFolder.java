package com.tdrhq.eyepatch.util;

import org.junit.rules.TemporaryFolder;

public class ExposedTemporaryFolder extends TemporaryFolder {
    @Override
    public void before() {
        try {
            super.before();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void after() {
        try {
            super.after();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
