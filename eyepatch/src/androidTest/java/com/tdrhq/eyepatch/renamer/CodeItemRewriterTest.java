// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.renamer;

import org.junit.Test;
import static org.junit.Assert.*;

public class CodeItemRewriterTest {

    @Test
    public void testWithoutConstStringIsUnchanged() throws Throwable {
        String insn = "0000 000e";
        CodeItem codeItem = new CodeItem(null);
        setInsn(codeItem, insn);
        CodeItemRewriter.updateStringIdsInCodeItem(null, codeItem);
        assertEquals(insn, formatIns(codeItem.insns));
    }

    private void setInsn(CodeItem codeItem, String insn) {
        codeItem.insns = makeIns(insn);
        codeItem.insnsSize = codeItem.insns.length;
    }

    short[] makeIns(String s) {
        String[] parts = s.split("\\s+");
        short[] ret = new short[parts.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = Short.parseShort(parts[i], 16);
        }
        return ret;
    }

    String formatIns(short[] insn) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < insn.length; i++) {
            builder.append(String.format("%04x ", insn[i]));
        }
        return builder.toString().trim();
    }
}
