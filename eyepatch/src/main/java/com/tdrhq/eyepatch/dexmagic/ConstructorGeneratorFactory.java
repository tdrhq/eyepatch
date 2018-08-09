// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import com.android.dx.Code;
import com.android.dx.Local;
import com.android.dx.TypeId;
import com.tdrhq.eyepatch.iface.SuperInvocation;

public class ConstructorGeneratorFactory {
    public ConstructorGenerator newInstance(TypeId<?> typeId,
                                            Class original,
                                            Local<SuperInvocation> superInvocation,
                                            Code code) {
        return new ConstructorGenerator(typeId, original, superInvocation, code);
    }
}
