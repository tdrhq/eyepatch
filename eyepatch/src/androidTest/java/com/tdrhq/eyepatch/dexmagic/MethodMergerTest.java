// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction10x;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction11x;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MethodMergerTest {
    @Before
    public void test() throws Throwable {
        Method template = makeMethod(
                new ImmutableInstruction11x(Opcode.THROW, 0));

        Method realMethod = makeMethod(
                new ImmutableInstruction10x(Opcode.RETURN_VOID));
    }

    @Test
    public void testpreconditions() throws Throwable {

    }

    private Method makeMethod(Instruction... instructions) {
        return new ImmutableMethod(
                "L/com/foo/Foo;",
                "bar",
                ImmutableList.of(),
                "V",
                0,
                ImmutableSet.of(),
                new ImmutableMethodImplementation(
                        1,
                        Lists.newArrayList(instructions),
                        Lists.newArrayList(),
                        Lists.newArrayList()));
    }

}
