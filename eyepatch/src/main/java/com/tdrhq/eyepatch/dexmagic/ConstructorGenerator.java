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

    public void declareLocals(TypeId<?> typeId, Class original, Code code) {
        TypeId parent = TypeId.get(original.getSuperclass());
        Constructor parentConstructor = getEasiestConstructor(original.getSuperclass());
        Class[] parameterTypes = parentConstructor.getParameterTypes();
        parentArgs = new Local[parameterTypes.length];
        currentClass = code.newLocal(TypeId.get(Class.class));
        getDefaultReturnVal = code.newLocal(TypeId.OBJECT);
    }

    public void invokeSuper(TypeId<?> typeId, Class original, Code code) {
        TypeId parent = TypeId.get(original.getSuperclass());
        declareLocals(typeId, original, code);
        Constructor parentConstructor = getEasiestConstructor(original.getSuperclass());
        Class[] parameterTypes = parentConstructor.getParameterTypes();
        TypeId[] argTypes = new TypeId[parameterTypes.length];

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
