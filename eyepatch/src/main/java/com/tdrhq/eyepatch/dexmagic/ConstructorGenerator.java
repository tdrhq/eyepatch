package com.tdrhq.eyepatch.dexmagic;

import com.android.dx.Code;
import com.android.dx.Local;
import com.android.dx.MethodId;
import com.android.dx.TypeId;

import java.lang.reflect.Constructor;

public class ConstructorGenerator {

    private static Constructor getEasiestConstructor(Class klass) {
        Constructor best = null;
        int bestCost = Integer.MAX_VALUE;
        for (Constructor cons : klass.getDeclaredConstructors()) {
            if (getConstructorCost(cons) < bestCost) {
                best = cons;
            }
        }
        return best;
    }

    private static int getConstructorCost(Constructor cons) {
        return cons.getParameterTypes().length;
    }

    public void invokeSuper(TypeId<?> typeId, TypeId parent, Class original, Code code) {
        // Since this is the first method, we can still create locals
        Constructor parentConstructor = getEasiestConstructor(original.getSuperclass());
        Class[] parameterTypes = parentConstructor.getParameterTypes();
        TypeId[] argTypes = new TypeId[parameterTypes.length];
        Local[] parentArgs = new Local[parameterTypes.length];
        Local<Class> currentClass = code.newLocal(TypeId.get(Class.class));
        Local<Object> getDefaultReturnVal = code.newLocal(TypeId.OBJECT);

        for (int i = 0; i < parameterTypes.length; i++) {
            argTypes[i] = TypeId.get(parameterTypes[i]);
            parentArgs[i] = code.newLocal(argTypes[i]);
        }

        for (int i = 0; i < parameterTypes.length; i++) {
            if (!Primitives.isPrimitive(parameterTypes[i])) {
                TypeId<?> staticInvoker = TypeId.get(StaticInvocationHandler.class);
                code.loadConstant(currentClass, parameterTypes[i]);
                MethodId getDefaultConstructorArg =
                        staticInvoker.getMethod(
                                TypeId.OBJECT,
                                "getDefaultConstructorArg",
                                TypeId.get(Class.class));
                code.invokeStatic(getDefaultConstructorArg, getDefaultReturnVal, currentClass);
                code.cast(parentArgs[i], getDefaultReturnVal);
            } else {
                code.loadConstant(parentArgs[i], null);
            }
        }

        code.invokeDirect(parent.getConstructor(argTypes),
                          null, code.getThis(typeId), parentArgs);
    }
}
