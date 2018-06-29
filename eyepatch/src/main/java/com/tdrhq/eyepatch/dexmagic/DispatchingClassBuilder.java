package com.tdrhq.eyepatch.dexmagic;

interface DispatchingClassBuilder {
    Class wrapClass(Class realClass, ClassLoader classLoader);
}
