// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import com.android.dx.Code;
import com.android.dx.TypeId;

public class ConstructorGeneratorFactory {
    public ConstructorGenerator newInstance(TypeId<?> typeId, Class original, Code code) {
        return new ConstructorGenerator(typeId, original, code);
    }
}
