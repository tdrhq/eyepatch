// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.iface;

public interface StaticVerificationHandler {
    void verifyStatic(Class klass);
    void resetStatic(Class klass);
}
