// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import com.google.common.collect.Lists;
import java.util.List;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.util.MethodUtil;

public class MethodMerger {
    private Method oldMethod;
    private Method realMethod;

    public MethodMerger(Method oldMethod, Method realMethod) {
        this.oldMethod = oldMethod;
        this.realMethod = realMethod;
    }

    public Method merge() {
        MethodImplementation impl = oldMethod.getImplementation();
        int params = MethodUtil.getParameterRegisterCount(oldMethod);
        List<Instruction> instructions = Lists.newArrayList(impl.getInstructions());
        // get rid of the final instruction
        Instruction last = instructions.get(instructions.size() - 1);

        if (last.getOpcode() != Opcode.THROW) {
            throw new IllegalStateException("this doesn't look like a template now does it");
        }

        instructions.remove(instructions.size() - 1);

        for (Instruction ins : realMethod.getImplementation().getInstructions()) {
            instructions.add(ins);
        }

        impl = new ImmutableMethodImplementation(
                impl.getRegisterCount() +
                realMethod.getImplementation().getRegisterCount() - params,
                instructions,
                impl.getTryBlocks(),
                impl.getDebugItems());

        return new ImmutableMethod(
                oldMethod.getDefiningClass(),
                oldMethod.getName(),
                oldMethod.getParameters(),
                oldMethod.getReturnType(),
                oldMethod.getAccessFlags(),
                oldMethod.getAnnotations(),
                impl);
    }
}
