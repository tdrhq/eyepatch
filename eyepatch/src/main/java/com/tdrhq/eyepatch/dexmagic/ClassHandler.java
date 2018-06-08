// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

public interface ClassHandler {
    /**
     * Handles the invocation.
     *
     * Note that constructor calls are invoked with the
     * "__construct__" method name, but only after super() is called.
     */
    public Object handleInvocation(Invocation invocation);

    /**
     * Get the class this handler is responsible for. A ClassHandler
     * can only be responsible for one single class.
     */
    public Class getResponsibility();
}
