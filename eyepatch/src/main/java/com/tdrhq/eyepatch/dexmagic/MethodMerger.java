// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import android.util.Log;
import com.google.common.collect.Lists;
import java.util.List;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction10x;
import org.jf.dexlib2.iface.instruction.formats.Instruction11n;
import org.jf.dexlib2.iface.instruction.formats.Instruction11x;
import org.jf.dexlib2.iface.instruction.formats.Instruction21c;
import org.jf.dexlib2.iface.instruction.formats.Instruction21s;
import org.jf.dexlib2.iface.instruction.formats.Instruction22c;
import org.jf.dexlib2.iface.instruction.formats.Instruction22t;
import org.jf.dexlib2.iface.instruction.formats.Instruction22x;
import org.jf.dexlib2.iface.instruction.formats.Instruction23x;
import org.jf.dexlib2.iface.instruction.formats.Instruction35c;
import org.jf.dexlib2.iface.instruction.formats.Instruction3rc;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction10x;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction11n;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction11x;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction21c;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction21s;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction22c;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction22t;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction22x;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction23x;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction35c;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction3rc;
import org.jf.dexlib2.util.MethodUtil;

public class MethodMerger {
    private Method template;
    private Method realMethod;

    public MethodMerger(Method template, Method realMethod) {
        this.template = template;
        this.realMethod = realMethod;
    }


    private int renameRegister(int reg) {
        if (reg < 16) {
            throw new RuntimeException("Can't renumber a register < 16, because it's likely to affect the instruction itself: " +
                                       reg + " for method " + template);

        }

        return realMethod.getImplementation().getRegisterCount() + reg;
    }

    public Method merge() {
        int params = MethodUtil.getParameterRegisterCount(template);
        MethodImplementation impl = template.getImplementation();
        List<Instruction> instructions = Lists.newArrayList();
        // get rid of the final instruction

        for (Instruction ins : template.getImplementation().getInstructions()) {
            // we're going to rename the registers in each instruction
            Log.i("MethodMerger", "working on: " + ins + ", " + ins.getOpcode());
            if (ins instanceof Instruction21c) {
                Instruction21c ins1 = (Instruction21c) ins;
                instructions.add(new ImmutableInstruction21c(
                                         ins.getOpcode(),
                                         renameRegister(ins1.getRegisterA()),
                                         ins1.getReference()));
            } else if (ins instanceof Instruction35c) {
                Instruction35c ins1 = (Instruction35c) ins;
                instructions.add(
                        new ImmutableInstruction35c(
                                ins1.getOpcode(),
                                ins1.getRegisterCount(),
                                renameRegister(ins1.getRegisterC()),
                                renameRegister(ins1.getRegisterD()),
                                renameRegister(ins1.getRegisterE()),
                                renameRegister(ins1.getRegisterF()),
                                renameRegister(ins1.getRegisterG()),
                                ins1.getReference()));

            } else if (ins instanceof Instruction11x) {
                Instruction11x ins1 = (Instruction11x) ins;

                instructions.add(
                        new ImmutableInstruction11x(
                                ins1.getOpcode(),
                                renameRegister(ins1.getRegisterA())));
            } else if(ins instanceof Instruction11n) {
                Instruction11n ins1 = (Instruction11n) ins;

                instructions.add(
                        new ImmutableInstruction11n(
                                ins1.getOpcode(),
                                renameRegister(ins1.getRegisterA()),
                                ins1.getNarrowLiteral()));
            } else if (ins instanceof Instruction22c) {
                Instruction22c ins1 = (Instruction22c) ins;
                instructions.add(
                        new ImmutableInstruction22c(
                                ins1.getOpcode(),
                                renameRegister(ins1.getRegisterA()),
                                renameRegister(ins1.getRegisterB()),
                                ins1.getReference()));
            } else if (ins instanceof Instruction22t) {
                Instruction22t ins1 = (Instruction22t) ins;
                instructions.add(
                        new ImmutableInstruction22t(
                                ins1.getOpcode(),
                                renameRegister(ins1.getRegisterA()),
                                renameRegister(ins1.getRegisterB()),
                                // TODO: does offset need to change?
                                ins1.getCodeOffset()));
            } else if (ins instanceof Instruction21s) {
                Instruction21s ins1 = (Instruction21s) ins;
                instructions.add(
                        new ImmutableInstruction21s(
                                ins1.getOpcode(),
                                renameRegister(ins1.getRegisterA()),
                                ins1.getNarrowLiteral()));
            } else if (ins instanceof Instruction22x) {
                Instruction22x ins1 = (Instruction22x) ins;
                instructions.add(
                        new ImmutableInstruction22x(
                                ins1.getOpcode(),
                                renameRegister(ins1.getRegisterA()),
                                renameRegister(ins1.getRegisterB())));
            } else if (ins instanceof Instruction3rc) {
                Instruction3rc ins1 = (Instruction3rc) ins;
                instructions.add(
                        new ImmutableInstruction3rc(
                                ins1.getOpcode(),
                                renameRegister(ins1.getStartRegister()),
                                ins1.getRegisterCount(),
                                ins1.getReference()));

            } else if (ins instanceof Instruction10x) {
                Instruction10x ins1 = (Instruction10x) ins;
                instructions.add(
                        new ImmutableInstruction10x(
                                ins1.getOpcode()));
            } else if (ins instanceof Instruction23x) {
                Instruction23x ins1 = (Instruction23x) ins;
                instructions.add(
                        new ImmutableInstruction23x(
                                ins1.getOpcode(),
                                renameRegister(ins1.getRegisterA()),
                                renameRegister(ins1.getRegisterB()),
                                renameRegister(ins1.getRegisterC())));
            } else {
                throw new UnsupportedOperationException("could not rename instruction type: " + ins.getClass());
            }
        }

        //Instruction last = instructions.get(instructions.size() - 1);
        // if (last.getOpcode() != Opcode.THROW) {
        //     throw new IllegalStateException("this doesn't look like a template now does it");
        // }

        // instructions.remove(instructions.size() - 1);

        // for (Instruction ins : realMethod.getImplementation().getInstructions()) {
        //     instructions.add(ins);
        // }

        impl = new ImmutableMethodImplementation(
                impl.getRegisterCount() +
                realMethod.getImplementation().getRegisterCount(),
                instructions,
                impl.getTryBlocks(),
                impl.getDebugItems());

        return new ImmutableMethod(
                template.getDefiningClass(),
                template.getName(),
                template.getParameters(),
                template.getReturnType(),
                template.getAccessFlags(),
                template.getAnnotations(),
                impl);
    }
}
