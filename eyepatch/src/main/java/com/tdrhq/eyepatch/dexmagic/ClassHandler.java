// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

public interface ClassHandler {
    public Object handleInvocation(Invocation invocation);

    /**
     * Get the class this handler is responsible for. A ClassHandler
     * can only be responsible for one single class.
     */
    public Class getResponsibility();
}
