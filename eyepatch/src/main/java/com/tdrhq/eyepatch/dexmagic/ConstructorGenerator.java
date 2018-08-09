package com.tdrhq.eyepatch.dexmagic;

import com.android.dx.Code;
import com.android.dx.Comparison;
import com.android.dx.Label;
import com.android.dx.Local;
import com.android.dx.MethodId;
import com.android.dx.TypeId;
import com.tdrhq.eyepatch.iface.SuperInvocation;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates the contents of a constructor for an EyePatch class.
 *
 * In particular this is only upto the super() call.
 */
public class ConstructorGenerator {

    private Local<Class> currentClass = null;
    private Local<Object> getDefaultReturnVal = null;

    // The value stored in this superinvocation is going to determine
    // what super constructor to call.
    private Local<SuperInvocation> superInvocation;

    private TypeId<?> mTypeId;
    private Class mParent;
    private Code mCode;
    private Constructor[] constructors;
    private Label endLabel;

    // the constructor id as selected by the the SuperInvocation
    private Local<Integer> expectedConstructorId;

    // the next constructor id we're comparing against.
    private Local<Integer> nextConstructorId;
    private Local<RuntimeException> runtimeException;

    // For each constructor, for each arg, we declare a new and appropriate local
    private Map<Constructor, Local[]> parentArgs = new HashMap<>();
    private Map<Constructor, Label> labelToSuperInvocation = new HashMap<>();

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
        constructors = mParent.getDeclaredConstructors();
        TypeId parent = TypeId.get(mParent);

        if (constructors.length == 0) {
            throw new IllegalStateException("huh I didn't think zero constructors could exist");
        }

        for (int i = 0; i < constructors.length; i++) {
            Constructor parentConstructor = constructors[i];
            parentArgs.put(parentConstructor, declareLocalsFor(parentConstructor));
            labelToSuperInvocation.put(parentConstructor, new Label());
        }
        endLabel = new Label();
        expectedConstructorId = mCode.newLocal(TypeId.INT);
        nextConstructorId = mCode.newLocal(TypeId.INT);
        runtimeException = mCode.newLocal(TypeId.get(RuntimeException.class));
    }

    private Local[] declareLocalsFor(Constructor parentConstructor) {
        Class[] parameterTypes = parentConstructor.getParameterTypes();
        Local[] parentArgs = new Local[parameterTypes.length];
        currentClass = mCode.newLocal(TypeId.get(Class.class));
        getDefaultReturnVal = mCode.newLocal(TypeId.OBJECT);

        for (int i = 0; i < parameterTypes.length; i++) {
            parentArgs[i] = mCode.newLocal(TypeId.get(parameterTypes[i]));
        }
        return parentArgs;
    }

    public void invokeSuper() {
        jumpToInvocation();
        for (Constructor constructor : constructors) {
            mCode.mark(labelToSuperInvocation.get(constructor));
            invokeSuperFor(constructor);
        }
        mCode.mark(endLabel);
    }

    /**
     * Use the SuperInvocation to determine where to jump to.
     */
    private void jumpToInvocation() {
        MethodId getConsId = TypeId.get(SuperInvocation.class)
                .getMethod(
                        TypeId.INT,
                        "getConstructorId",
                        TypeId.get(SuperInvocation.class));
        mCode.invokeStatic(getConsId, expectedConstructorId, superInvocation);
        for (Constructor constructor : constructors) {
            int nextId = SuperInvocation.getConstructorId(constructor.getParameterTypes());
            mCode.loadConstant(nextConstructorId, nextId);
            mCode.compare(Comparison.EQ, labelToSuperInvocation.get(constructor),
                         nextConstructorId, expectedConstructorId);
        }

        mCode.newInstance(runtimeException, TypeId.get(RuntimeException.class).getConstructor());
        mCode.throwValue(runtimeException);
    }

    private void invokeSuperFor(Constructor parentConstructor) {
        TypeId parent = TypeId.get(mParent);
        Class[] parameterTypes = parentConstructor.getParameterTypes();
        TypeId[] argTypes = new TypeId[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            argTypes[i] = TypeId.get(parameterTypes[i]);
        }

        Local[] parentArgsForThisConstructor = parentArgs.get(parentConstructor);
        for (int i = 0; i < parameterTypes.length; i++) {
            Local target = parentArgsForThisConstructor[i];
            if (!Primitives.isPrimitive(parameterTypes[i])) {
                TypeId<?> constructorGenerator = TypeId.get(ConstructorGenerator.class);
                mCode.loadConstant(currentClass, parameterTypes[i]);
                MethodId getDefaultConstructorArg =
                        constructorGenerator.getMethod(
                                TypeId.OBJECT,
                                "getDefaultConstructorArg",
                                TypeId.get(Class.class));
                mCode.invokeStatic(getDefaultConstructorArg, getDefaultReturnVal, currentClass);
                mCode.cast(target, getDefaultReturnVal);
            } else {
                mCode.loadConstant(target, null);
            }
        }

        mCode.invokeDirect(parent.getConstructor(argTypes),
                           null, mCode.getThis(mTypeId), parentArgsForThisConstructor);
        mCode.jump(endLabel);
    }

    public static Object getDefaultConstructorArg(
            Class argType) {
        if (argType == String.class) {
            return "";
        }

        return null;
    }
}
