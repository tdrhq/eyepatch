// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import com.android.dx.*;
import com.android.dx.rop.code.AccessFlags;
import com.tdrhq.eyepatch.iface.Dispatcher;
import com.tdrhq.eyepatch.iface.GeneratedMethod;
import com.tdrhq.eyepatch.iface.SuperInvocation;
import com.tdrhq.eyepatch.util.Checks;
import com.tdrhq.eyepatch.util.ClassLoaderIntrospector;
import com.tdrhq.eyepatch.util.Sorter;
import com.tdrhq.eyepatch.util.Util;
import dalvik.system.DexFile;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DexFileGenerator {

    public static DebugPrinter debugPrinter = null;
    public static int SYNTHETIC = 0x1000; // hidden in Modifiers

    public interface DebugPrinter {
        public void print(Class klass, File dexFile);
    }

    private File mDataDir;
    private int counter = 0;
    private ConstructorGeneratorFactory constructorGeneratorFactory;
    private Merger merger = new Merger();

    public DexFileGenerator(File dataDir,
                            ConstructorGeneratorFactory mConstructorGeneratorFactory) {
        mDataDir = dataDir;
        constructorGeneratorFactory = mConstructorGeneratorFactory;
    }

    public DexFile generate(Class realClass) {
        try {
            int suffix = (++counter);
            File mergedOf = new File(mDataDir, "EPG_merged" + suffix + ".dex");
            File realDexFile = ClassLoaderIntrospector.getDefiningDexFile(realClass);
            FileInputStream real = new FileInputStream(realDexFile);

            if (realClass.isInterface()) {
                merger.copyDexFile(
                        realClass,
                        real,
                        mergedOf);

            } else {

                DexMaker dexmaker = buildDexMaker(realClass.getName(), realClass);
                ByteArrayInputStream template = new ByteArrayInputStream(dexmaker.generate());

                try {
                    merger.mergeDexFile(
                            template,
                            real,
                            mergedOf);
                } finally {
                    template.close();
                    real.close();
                }

                if (debugPrinter != null) {
                    debugPrinter.print(realClass, mergedOf);
                }
            }

            return Util.loadDexFile(mergedOf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private DexMaker buildDexMaker(String name, Class original) {
        DexMaker dexmaker = new DexMaker();
        TypeId<?> typeId = Util.createTypeIdForName(name);
        List<TypeId<?>> interfaces = new ArrayList<>();
        for (Type iface : original.getInterfaces()) {
            if (!(iface instanceof Class)) {
                continue;
            }

            interfaces.add(TypeId.get((Class) iface));
        }

        Class parentClass = original.getSuperclass();
        dexmakerDeclare(
                dexmaker, typeId,
                name + ".generated",
                Modifier.PUBLIC,
                TypeId.get(parentClass),
                interfaces.toArray(new TypeId<?>[0]));

        generatePartsOfClass(original, dexmaker, typeId);
        return dexmaker;
    }

    private void generatePartsOfClass(Class original, DexMaker dexmaker, TypeId<?> typeId) {
        for (Field field : original.getDeclaredFields()) {
            if ((field.getModifiers() & SYNTHETIC) != 0) {
                continue;
            }
            generateField(dexmaker, field, typeId);
        }

        for (Constructor constructor : Sorter.sortConstructors(original.getDeclaredConstructors()) ){
            generateConstructor(dexmaker, constructor, typeId, original);
            generateHandledConstructor(dexmaker, constructor, typeId, original);
        }

        for (Method methodTemplate : Sorter.sortMethods(original.getDeclaredMethods())) {
            if ((methodTemplate.getModifiers() & SYNTHETIC) != 0) {
                // unsupported, we'll let the merge phase fix it
                continue;
            }
            generateMethod(dexmaker, methodTemplate, typeId, original);
        }

        for (Method superMethod : Sorter.sortMethods(getSuperMethodsToDeclare(original))) {
            generateSuperMethod(dexmaker, superMethod, typeId, original);
        }
    }

    private void dexmakerDeclare(DexMaker dexmaker, TypeId<?> typeId, String s, int modifiers, TypeId parentClass, TypeId<?>[] interfaces) {
        dexmaker.declare(typeId, s, fixModifiers(modifiers), parentClass, interfaces);
    }

    public List<Method> getSuperMethodsToDeclare(Class klass) {
        List<Method> ret = new ArrayList<>();
        for (Method method : klass.getDeclaredMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) &&
                !Modifier.isPrivate(method.getModifiers())) {
                ret.add(method);
            }
        }

        return ret;
    }

    private static void generateField(DexMaker dexmaker, Field field, TypeId<?> typeId) {
        FieldId<?, ?> fieldId = typeId.getField(
                TypeId.get(field.getType()),
                field.getName());
        int modifiers = field.getModifiers();
        Object initialValue = null;
        if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
            field.setAccessible(true);
            try {
                initialValue = field.get(null);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        dexmakerDeclareField(dexmaker, fieldId, modifiers, initialValue);
    }

    private static void dexmakerDeclareField(DexMaker dexmaker, FieldId<?, ?> fieldId, int modifiers, Object initialValue) {
        dexmaker.declare(fieldId, fixModifiers(modifiers), initialValue);
    }

    private <D> void generateConstructor(DexMaker dexmaker, Constructor constructor, final TypeId<D> typeId, Class original) {
        int modifiers = constructor.getModifiers();
        TypeId returnType = TypeId.VOID;
        Class[] parameterTypes = constructor.getParameterTypes();
        TypeId[] arguments = new TypeId[parameterTypes.length];
        for (int i = 0 ;i < parameterTypes.length; i++) {
            arguments[i] = TypeId.get(parameterTypes[i]);
        }
        MethodId cons = typeId.getConstructor(arguments);
        Code  code = declareMethod(dexmaker, cons, Modifier.PUBLIC);
        Locals locals = new Locals(code, returnType);
        Local<SuperInvocation> superInvocation = code.newLocal(TypeId.get(SuperInvocation.class));
        Local<Class> thisClass = code.newLocal(TypeId.get(Class.class));

        code.loadConstant(thisClass, original.getSuperclass());
        MethodId getEasiestInvocation = TypeId.get(SuperInvocation.class)
                .getMethod(
                        TypeId.get(SuperInvocation.class),
                        "getEasiestInvocation",
                        TypeId.get(Class.class));

        code.invokeStatic(getEasiestInvocation, superInvocation, thisClass);

        generateInvokeWithoutReturn(code, typeId, returnType, parameterTypes, original, modifiers | Modifier.STATIC, EyePatchClassBuilder.PRE_CONSTRUCT, locals);

        arguments = Arrays.copyOf(arguments, arguments.length + 1);
        arguments[arguments.length - 1] = TypeId.get(SuperInvocation.class);
        MethodId handledConstructor = typeId.getConstructor(arguments);

        Local<?>[] handledParams = new Local<?>[arguments.length];
        for (int i = 0; i < arguments.length -1; i ++) {
            handledParams[i] = code.getParameter(i, arguments[i]);
        }
        handledParams[arguments.length -1] = superInvocation;
        code.invokeDirect(
                handledConstructor,
                null,
                code.getThis(typeId),
                handledParams);
        code.returnVoid();

        arguments = Arrays.copyOf(arguments, arguments.length - 1);
        generateBypassLabel(
                code,
                typeId,
                TypeId.VOID,
                "<init>",
                arguments,
                modifiers,
                locals);

    }

    private void generateHandledConstructor(DexMaker dexmaker, Constructor constructor, final TypeId<?> typeId, Class original) {
        int modifiers = constructor.getModifiers();
        TypeId returnType = TypeId.VOID;
        Class[] parameterTypes = constructor.getParameterTypes();
        parameterTypes = Arrays.copyOf(parameterTypes, parameterTypes.length + 1);
        parameterTypes[parameterTypes.length - 1] = SuperInvocation.class;

        TypeId[] arguments = new TypeId[parameterTypes.length];
        for (int i = 0 ;i < parameterTypes.length; i++) {
            arguments[i] = TypeId.get(parameterTypes[i]);
        }

        MethodId cons = typeId.getConstructor(arguments);
        Code  code = declareMethod(dexmaker, cons, Modifier.PUBLIC);
        Locals locals = new Locals(code, returnType);
        Local<SuperInvocation> superInvocation = code.getParameter(parameterTypes.length - 1, TypeId.get(SuperInvocation.class));
        Local<Class> thisClass = code.newLocal(TypeId.get(Class.class));

        ConstructorGenerator constructorGenerator = constructorGeneratorFactory
                .newInstance(typeId, original.getSuperclass(), superInvocation, code);
        constructorGenerator.declareLocals();

        constructorGenerator.invokeSuper();

        // funny... now before I actually make a __construct__
        // invocation, let's hide that SuperInvocation argument
        parameterTypes = Arrays.copyOf(parameterTypes, parameterTypes.length - 1);

        generateMethodContentsInternal(code, typeId, returnType, parameterTypes, original, modifiers, EyePatchClassBuilder.CONSTRUCT, locals);
        generateUnsupportedLabel(code, locals);
    }

    private void generateMethod(DexMaker dexmaker, Method methodTemplate, TypeId<?> typeId, Class original) {
        String methodName = methodTemplate.getName();
        int modifiers = methodTemplate.getModifiers();
        TypeId returnType = TypeId.get(methodTemplate.getReturnType());
        Class[] parameterTypes = methodTemplate.getParameterTypes();
        TypeId[] arguments = new TypeId[parameterTypes.length];
        for (int i = 0 ;i < parameterTypes.length; i++) {
            arguments[i] = TypeId.get(parameterTypes[i]);
        }
        MethodId foo = typeId.getMethod(returnType, methodName, arguments);

        Code code = declareMethod(dexmaker, foo, modifiers);
        Locals locals = new Locals(code, returnType);

        generateMethodContentsInternal(code, typeId, returnType, parameterTypes, original, modifiers, methodName, locals);

        generateBypassLabel(code, typeId, returnType, methodName, arguments, modifiers, locals);
    }

    /**
     * For a method foo(), generate __super__foo() that does nothing but call super.foo().
     */
    private void generateSuperMethod(DexMaker dexmaker, Method methodTemplate, TypeId<?> typeId, Class original) {
        String methodName = "__super__" + methodTemplate.getName();
        int modifiers = methodTemplate.getModifiers();
        TypeId returnType = TypeId.get(methodTemplate.getReturnType());
        Class[] parameterTypes = methodTemplate.getParameterTypes();
        TypeId[] arguments = new TypeId[parameterTypes.length];
        for (int i = 0 ;i < parameterTypes.length; i++) {
            arguments[i] = TypeId.get(parameterTypes[i]);
        }
        MethodId foo = typeId.getMethod(returnType, methodName, arguments);
        MethodId originalFoo = typeId.getMethod(returnType, methodTemplate.getName(), arguments);

        Code code = declareMethod(dexmaker, foo, modifiers);
        Locals locals = new Locals(code, returnType);

        Local[] argumentLocals = new Local[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            argumentLocals[i] = code.newLocal(arguments[i]);
            argumentLocals[i] = code.getParameter(i, arguments[i]);
        }


        code.invokeSuper(originalFoo,
                         returnType == TypeId.VOID ? null : locals.castedReturnValue,
                         code.getThis(typeId),
                         argumentLocals);

        if (returnType.equals(TypeId.VOID)) {
            code.returnVoid();
        } else {
            code.returnValue(locals.castedReturnValue);
        }
    }

    private Code declareMethod(DexMaker dexmaker, MethodId foo, int modifiers) {
        return dexmaker.declare(foo, fixModifiers(modifiers));
    }

    private static int fixModifiers(int modifiers) {
        return modifiers;
    }

    private void generateUnsupportedLabel(Code code, Locals locals) {
        code.mark(locals.defaultImplementation);
        code.newInstance(
                locals.uoe,
                TypeId.get(UnsupportedOperationException.class).getConstructor());
        code.throwValue(locals.uoe);
    }

    private <D,R> void generateBypassLabel(Code code, TypeId<D> typeId, TypeId<R> returnType, String methodName, TypeId<?>[] parameterTypes, int modifiers, Locals locals) {
        parameterTypes = Arrays.copyOf(parameterTypes, parameterTypes.length + 1);
        parameterTypes[parameterTypes.length - 1] = TypeId.get(Token.class);
        MethodId<D, R>  methodId = typeId.getMethod(
                returnType,
                methodName,
                parameterTypes);

        code.mark(locals.defaultImplementation);
        Local<?>[] params = new Local<?>[parameterTypes.length];
        for (int i = 0; i < params.length - 1; i++) {
            params[i] = code.getParameter(i, parameterTypes[i]);
        }
        code.loadConstant(locals.tmp, null);
        params[params.length - 1] = locals.tmp;

        Local<R> returnValue = null;
        if (returnType != TypeId.VOID) {
            returnValue = locals.castedReturnValue;
        }

        if (methodName.equals("<init>") || Modifier.isPrivate(modifiers)) {
            code.invokeDirect(
                    methodId,
                    returnValue,
                    code.getThis(typeId),
                    params);
        } else if (Modifier.isStatic(modifiers)) {
            code.invokeStatic(
                    methodId,
                    returnValue,
                    params);
        } else {
            code.invokeVirtual(
                    methodId,
                    returnValue,
                    code.getThis(typeId),
                    params);
        }

        if (returnType == TypeId.VOID) {
            code.returnVoid();
        } else {
            code.returnValue(locals.castedReturnValue);
        }
    }

    private static void generateMethodContentsInternal(Code code, TypeId typeId, TypeId returnType, Class[] parameterTypes, Class original, int modifiers, String methodName, Locals locals) {
        generateInvokeWithoutReturn(code, typeId, returnType, parameterTypes, original, modifiers, methodName, locals);

        if (returnType == TypeId.VOID) {
            code.returnVoid();
        } else {
            code.returnValue(locals.castedReturnValue);
        }
    }

    private static void generateInvokeWithoutReturn(Code code, TypeId typeId, TypeId returnType, Class[] parameterTypes, Class original, int modifiers, String methodName, Locals locals) {
        TypeId<?> staticInvoker = TypeId.get(Dispatcher.class);
        TypeId classType =  TypeId.get(Class.class);
        TypeId instance = TypeId.OBJECT;
        TypeId objectType = TypeId.get(Object.class);
        TypeId stringType = TypeId.get(String.class);
        TypeId argArType = TypeId.get(Class[].class);
        TypeId objectArType = TypeId.get(Object[].class);

        MethodId invokeStaticMethod = staticInvoker.getMethod(
                objectType,
                "invokeStatic",
                TypeId.get(GeneratedMethod.class),
                objectType,
                objectArType);

        code.loadConstant(locals.parameterLength, parameterTypes.length);
        code.loadConstant(locals.argTypes, null);

        buildCallerArray(locals.callerArgs, locals.parameterLength, locals.tmp, parameterTypes, code);
        code.loadConstant(locals.parameterLength, parameterTypes.length);
        buildArgArray(locals.argTypes, locals.parameterLength, parameterTypes, locals.tmp, code);

        code.loadConstant(locals.callerClass, original);
        Local<Object> instanceArg;
        if (Modifier.isStatic(modifiers)) {
            code.loadConstant(locals.instanceArg, null);
            instanceArg = locals.instanceArg;
        } else {
            instanceArg = code.getThis(typeId);
        }
        code.loadConstant(locals.callerMethod, methodName);
        MethodId<GeneratedMethod, GeneratedMethod> generatedMethodCreate = TypeId.get(GeneratedMethod.class)
                .getMethod(
                        TypeId.get(GeneratedMethod.class),
                        "create",
                        TypeId.get(Class.class),
                        TypeId.STRING,
                        TypeId.get(Class[].class));
        code.invokeStatic(generatedMethodCreate,
                          locals.generatedMethod,
                          locals.callerClass,
                          locals.callerMethod,
                          locals.argTypes);

        code.invokeStatic(
                invokeStaticMethod,
                locals.returnValue,
                locals.generatedMethod,
                instanceArg,
                locals.callerArgs);

        FieldId<?, ?> unhandledValueField =
                staticInvoker.getField(TypeId.OBJECT, "UNHANDLED");
        code.sget(unhandledValueField, locals.unhandledValue);
        code.compare(
                Comparison.EQ,
                locals.defaultImplementation,
                Checks.notNull(locals.returnValue),
                Checks.notNull(locals.unhandledValue));

        if (Primitives.isPrimitive(returnType)) {
            MethodId intValue = Primitives.getBoxedType(returnType)
                    .getMethod(
                            returnType,
                            Primitives.getUnboxFunction(returnType));
            code.cast(locals.boxedReturnValue, locals.returnValue);
            code.invokeVirtual(
                    intValue,
                    locals.castedReturnValue,
                    locals.boxedReturnValue);

        } else if (returnType != TypeId.VOID) {
            code.cast(locals.castedReturnValue, locals.returnValue);
        }
    }

    private static class Locals {
        Label defaultImplementation;
        Local<Object> returnValue;
        Local<Class> callerClass;
        Local instanceArg;
        Local<String> callerMethod;
        Local<Class[]> argTypes;
        Local<Object[]> callerArgs;
        Local castedReturnValue;
        Local<Integer> parameterLength;
        Local<Object> tmp;
        Local boxedReturnValue;
        Local<UnsupportedOperationException> uoe;
        Local<Object> unhandledValue;
        Local<GeneratedMethod> generatedMethod;

        public Locals(Code code, TypeId returnType) {
            defaultImplementation = new Label();

            returnValue = code.newLocal(TypeId.OBJECT);
            callerClass = code.newLocal(TypeId.get(Class.class));
            instanceArg = code.newLocal(TypeId.OBJECT);
            callerMethod = code.newLocal(TypeId.STRING);
            argTypes = code.newLocal(TypeId.get(Class[].class));
            callerArgs = code.newLocal(TypeId.get(Object[].class));
            castedReturnValue = code.newLocal(returnType);
            parameterLength = code.newLocal(TypeId.INT);
            tmp = code.newLocal(TypeId.OBJECT);
            uoe = code.newLocal(TypeId.get(UnsupportedOperationException.class));
            unhandledValue = code.newLocal(TypeId.OBJECT);
            generatedMethod = code.newLocal(TypeId.get(GeneratedMethod.class));

            boxedReturnValue = null;

            if (Primitives.isPrimitive(returnType) && returnType != TypeId.VOID) {
                boxedReturnValue = code.newLocal(Primitives.getBoxedType(returnType));
            }

        }
    }

    private static void buildArgArray(
            Local<Class[]> output,
            Local<Integer> parameterLength,
            Class[] parameterTypes,
            Local<Object> tmp,
            Code code) {
        code.newArray(output, parameterLength);
        for (int i = 0; i < parameterTypes.length; i++) {
            code.loadConstant(parameterLength, i);
            code.loadConstant(tmp, parameterTypes[i]);
            code.aput(output, parameterLength, tmp);
        }
    }

    private static void buildCallerArray(
            Local<Object[]> callerArgs, Local<Integer> parameterLength,
            Local<Object> tmp,
            Class[] parameterTypes, Code code) {
        code.newArray(callerArgs, parameterLength);
        for (int i = parameterTypes.length - 1; i>= 0; i--) {
            code.loadConstant(parameterLength, i);
            if (Primitives.isPrimitive(parameterTypes[i])) {
                code.newInstance(
                        tmp,
                        Primitives.getBoxedType(TypeId.get(parameterTypes[i])).getConstructor(
                                TypeId.get(parameterTypes[i])),
                        code.getParameter(i, TypeId.get(parameterTypes[i])));
            } else {
                code.cast(tmp, code.getParameter(i, TypeId.get(parameterTypes[i])));
            }
            code.aput(callerArgs, parameterLength, tmp);
        }
    }

}
