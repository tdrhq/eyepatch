package com.tdrhq.eyepatch.renamer;

import java.util.ArrayList;
import java.util.List;

class CodeItemRewriter {
    static interface StringIdProvider {
        int getUpdatedStringIndex(int originalIndex);
    }

    static void updateStringIdsInCodeItem(StringIdProvider dexFileReader, CodeItem codeItem) {
        List<Short> newInsns = new ArrayList<>(1000);

        for (int i = 0; i < codeItem.insnsSize; ) {
            int insn = codeItem.insns[i];
            int opcode = insn & 0xff;
            int insnLen = InsnFormat.getLength(opcode);

            if (insnLen > 5) {
                throw new IllegalStateException("this was unexpected");
            }

            if (opcode == 0x1a /* const-string */) {
                // For easier implementation we'll always change to const-string/jumbo
                int newIdx = dexFileReader.getUpdatedStringIndex(codeItem.insns[i+1]);

                if ((newIdx & 0xffff0000) != 0) {
                    // uh oh, we need to rewrite this to a jumbo instruction
                    newInsns.add((short) ((insn & 0xff00) | 0x1b));
                    newInsns.add((short) (newIdx & 0xffff));
                    newInsns.add((short) (newIdx >> 16));

                } else {
                    newInsns.add((short) (insn));
                    newInsns.add((short) newIdx);
                }
            }
            else if (opcode == 0x1b /* const-string/jumbo */) {
                int n1 = codeItem.insns[i+1];
                int n2 = codeItem.insns[i+2];

                int index = n1 | ( n2 << 16 );
                int newIdx = dexFileReader.getUpdatedStringIndex(index);

                newInsns.add((short) insn);
                newInsns.add((short) (newIdx & 0xffff));
                newInsns.add((short) (newIdx >> 16));
            } else {
                // copy whole instruction as is
                for (int j = 0; j < insnLen; j++) {
                    newInsns.add(codeItem.insns[i + j]);
                }
            }

            i += insnLen;
        }

        codeItem.insns = new short[newInsns.size()];
        for (int i = 0; i < newInsns.size(); i++) {
            codeItem.insns[i] = newInsns.get(i);
        }
        codeItem.insnsSize = codeItem.insns.length;
    }
}
