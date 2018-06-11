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
                bestCost = getConstructorCost(cons);
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

    // The value stored in this superinvocation is going to determine
    // what super constructor to call.
    private Local<SuperInvocation> superInvocation;

    private TypeId<?> mTypeId;
    private Class mParent;
    private Code mCode;

    public ConstructorGenerator(TypeId<?> typeId,
                                Class parent,
                                Local<SuperInvocation> superInvocation,
                                Code code) {
        mTypeId = typeId;
        mParent = parent;
        this.superInvocation = superInvocation;
        mCode = code;
    }

    public void declareLocals() {
        TypeId parent = TypeId.get(mParent);
        Constructor parentConstructor = getEasiestConstructor(mParent);
        Class[] parameterTypes = parentConstructor.getParameterTypes();
        parentArgs = new Local[parameterTypes.length];
        currentClass = mCode.newLocal(TypeId.get(Class.class));
        getDefaultReturnVal = mCode.newLocal(TypeId.OBJECT);

        for (int i = 0; i < parameterTypes.length; i++) {
            parentArgs[i] = mCode.newLocal(TypeId.get(parameterTypes[i]));
        }
    }

    public void invokeSuper() {
        TypeId parent = TypeId.get(mParent);
        Constructor parentConstructor = getEasiestConstructor(mParent);
        Class[] parameterTypes = parentConstructor.getParameterTypes();
        TypeId[] argTypes = new TypeId[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            argTypes[i] = TypeId.get(parameterTypes[i]);
        }

        for (int i = 0; i < parameterTypes.length; i++) {
            if (!Primitives.isPrimitive(parameterTypes[i])) {
                TypeId<?> constructorGenerator = TypeId.get(ConstructorGenerator.class);
                mCode.loadConstant(currentClass, parameterTypes[i]);
                MethodId getDefaultConstructorArg =
                        constructorGenerator.getMethod(
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

    public static Object getDefaultConstructorArg(
            Class argType) {
        if (argType == String.class) {
            return "";
        }

        return null;
    }
}
