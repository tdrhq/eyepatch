package com.tdrhq.eyepatch.iface;

import com.tdrhq.eyepatch.iface.Invocation;

public interface StaticInvocationHandler {
    Object handleInvocation(Invocation invocation) throws Exception;
}
