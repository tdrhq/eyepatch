package com.tdrhq.eyepatch.dexmagic;

import com.android.dx.Code;
import com.android.dx.Local;
import com.android.dx.MethodId;
import com.android.dx.TypeId;

import java.lang.reflect.Constructor;

/**
 * Generates the contents of a constructor for an EyePatch class.
 *
 * In particular this is only upto the super() call.
 */
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

    private Local[] parentArgs = null;
    private Local<Class> currentClass = null;
    private Local<Object> getDefaultReturnVal = null;

    private TypeId<?> mTypeId;
    private Class mParent;
    private Code mCode;

    public ConstructorGenerator(TypeId<?> typeId,
                                Class parent,
                                Code code) {
        mTypeId = typeId;
        mParent = parent;
        mCode = code;
    }

    public void declareLocals() {
        TypeId parent = TypeId.get(mParent);
        Constructor parentConstructor = getEasiestConstructor(mParent);
        Class[] parameterTypes = parentConstructor.getParameterTypes();
        parentArgs = new Local[parameterTypes.length];
        currentClass = mCode.newLocal(TypeId.get(Class.class));
        getDefaultReturnVal = mCode.newLocal(TypeId.OBJECT);
    }

    public void invokeSuper() {
        TypeId parent = TypeId.get(mParent);
        Constructor parentConstructor = getEasiestConstructor(mParent);
        Class[] parameterTypes = parentConstructor.getParameterTypes();
        TypeId[] argTypes = new TypeId[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            argTypes[i] = TypeId.get(parameterTypes[i]);
            parentArgs[i] = mCode.newLocal(argTypes[i]);
        }

        for (int i = 0; i < parameterTypes.length; i++) {
            if (!Primitives.isPrimitive(parameterTypes[i])) {
                TypeId<?> staticInvoker = TypeId.get(StaticInvocationHandler.class);
                mCode.loadConstant(currentClass, parameterTypes[i]);
                MethodId getDefaultConstructorArg =
                        staticInvoker.getMethod(
                                TypeId.OBJECT,
                                "getDefaultConstructorArg",
                                TypeId.get(Class.class));
                mCode.invokeStatic(getDefaultConstructorArg, getDefaultReturnVal, currentClass);
                mCode.cast(parentArgs[i], getDefaultReturnVal);
            } else {
                mCode.loadConstant(parentArgs[i], null);
            }
        }

        mCode.invokeDirect(parent.getConstructor(argTypes),
                          null, mCode.getThis(mTypeId), parentArgs);
    }
}
