package com.tdrhq.eyepatch.dexmagic;

import com.tdrhq.eyepatch.iface.Invocation;

public interface StaticInvocationHandler {
    Object handleInvocation(Invocation invocation);
}
