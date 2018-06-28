package com.tdrhq.eyepatch.renamer;

import java.util.ArrayList;
import java.util.List;

class CodeItemRewriter {
    static interface StringIdProvider {
        int getUpdatedStringIndex(int originalIndex);
    }

    static void updateStringIdsInCodeItem(StringIdProvider dexFileReader, CodeItem codeItem) {
        List<Short> newInsns = new ArrayList<>();

        for (int i = 0; i < codeItem.insnsSize; ) {
            int insn = codeItem.insns[i];
            int opcode = insn & 0xff;
            int insnLen = InsnFormat.getLength(opcode);

            if (opcode == 0x1a /* const-string */) {
                // For easier implementation we'll always change to const-string/jumbo
                newInsns.add((short) ((insn & 0xff00) | 0x1b));
                int newIdx = dexFileReader.getUpdatedStringIndex(codeItem.insns[i+1]);
                codeItem.insns[i+1] = (short) newIdx;
            }
            else if (opcode == 0x1b /* const-string/jumbo */) {
                throw new UnsupportedOperationException("TODO: big dex file, amiright?");
            } else {
                // copy whole instruction as is
                for (int j = 0; j < insnLen; j++) {
                    newInsns.add(codeItem.insns[i + j]);
                }
            }

            i += insnLen;
        }

    }
}
