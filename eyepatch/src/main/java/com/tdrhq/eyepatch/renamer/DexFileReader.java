// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.renamer;

import com.android.dex.Leb128;
import com.android.dex.Mutf8;
import com.android.dex.util.ByteInput;
import com.android.dx.dex.DexOptions;
import com.android.dx.dex.file.ClassDefItem;
import com.android.dx.dex.file.DexFile;
import com.android.dx.rop.cst.CstString;
import com.android.dx.rop.cst.CstType;
import com.android.dx.rop.type.StdTypeList;
import com.android.dx.rop.type.Type;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Reads a {@code DexFile} from the given file.
 *
 * This lets us modifiy the structures of the DexFile in order to
 * rewrite it as something else.
 */
public class DexFileReader {
    private File file;
    public DexFileReader(File file) {
        this.file = file;
    }

    HeaderItem headerItem = null;
    StringIdItem[] stringIdItems = null;
    RandomAccessFile raf;
    _ClassDefItem[] classDefItems = null;
    _TypeIdItem[] typeIdItems = null;

    public DexFile read() throws IOException {

        DexFile dexFile = new DexFile(new DexOptions());
        raf = new RandomAccessFile(file, "r");
        headerItem = new HeaderItem();
        headerItem.read();

        raf.seek(headerItem.stringIdsOff);
        stringIdItems = new StringIdItem[(int) headerItem.stringIdsSize];
        for (int i = 0; i < headerItem.stringIdsSize; i ++) {
            stringIdItems[i] = new StringIdItem();
            stringIdItems[i].read();
        }

        raf.seek(headerItem.typeIdsOff);
        typeIdItems = new _TypeIdItem[headerItem.typeIdsSize];
        for (int i = 0; i < headerItem.typeIdsSize; i ++) {
            typeIdItems[i] = new _TypeIdItem();
            typeIdItems[i].read();
        }

        raf.seek(headerItem.classDefsOff);
        classDefItems = new _ClassDefItem[(int) headerItem.classDefsSize];
        for (int i = 0; i < headerItem.classDefsSize; i++) {
            classDefItems[i] = new _ClassDefItem();
            classDefItems[i].read();
            dexFile.add(classDefItems[i].toClassDefItem());
        }

        return dexFile;
    }

    String getString(long idx) throws IOException {
        return stringIdItems[(int) idx].getString();
    }

    class HeaderItem implements Readable {
        long stringIdsSize;
        long stringIdsOff;
        long classDefsSize;
        long classDefsOff;
        int typeIdsSize;
        int typeIdsOff;
        int protoIdsSize;
        int protoIdsOff;
        int fieldIdsSize;
        int fieldIdsOff;
        int methodIdsSize;
        int methodIdsOff;

        public void read() throws IOException {
            raf.seek(0);
            raf.skipBytes(8); // magic
            raf.readInt();    // checksum
            raf.skipBytes(20); // signature
            for (int i = 0; i < 6; i++) {
                raf.readInt(); // bunch of stuff
            }
            stringIdsSize = readUInt();
            stringIdsOff = readUInt();

            typeIdsSize = readUInt();
            typeIdsOff = readUInt();

            protoIdsSize = readUInt();
            protoIdsOff = readUInt();

            fieldIdsSize = readUInt();
            fieldIdsOff = readUInt();

            methodIdsSize = readUInt();
            methodIdsOff = readUInt();

            classDefsSize = readUInt();
            classDefsOff = readUInt();
        }
    }

    class _TypeIdItem implements Readable {
        int descriptorIdx; // a  pointer to string_ids

        public void read() throws IOException {
            descriptorIdx = readUInt();
        }

        public String getString() throws IOException{
            return DexFileReader.this.getString(descriptorIdx);
        }
    }

    private String addSuffix(String inputType) {
        return inputType.replace(";", "_suffix;");
    }

    class _EncodedField implements Readable {
        int fieldIdxDiff;
        int accessFlags;

        public void read() throws IOException {
            fieldIdxDiff = readULeb128();
            accessFlags =readULeb128();
        }
    }

    class _EncodedMethod implements Readable {
        int methodIdxDiff;
        int accessFlags;
        int codeOff;

        public void read() throws IOException {
            methodIdxDiff = readULeb128();
            accessFlags = readULeb128();
            codeOff = readULeb128();
        }
    }

    class _ClassDefItem implements Readable {
        int classIdx;
        int accessFlags;
        int superclassIdx;
        int interfacesOff;
        int sourceFileIdx;
        int annotationsOff;
        int classDataOff;
        int staticValuesOff;

        class _ClassDataItem {
            int staticFieldsSize;
            int instanceFieldsSize;
            int directMethodsSize;
            int virtualMethodsSize;

            _EncodedField[] staticFields;
            _EncodedField[] instanceFields;
            _EncodedMethod[] directMethods;
            _EncodedMethod[] virtualMethods;

            public void read() throws IOException {
                staticFieldsSize = readULeb128();
                instanceFieldsSize = readULeb128();
                directMethodsSize = readULeb128();
                virtualMethodsSize = readULeb128();

                staticFields = readEncodedFields(staticFieldsSize);
                instanceFields = readEncodedFields(instanceFieldsSize);

                directMethods = readEncodedMethods(directMethodsSize);
                virtualMethods = readEncodedMethods(virtualMethodsSize);
            }

            _EncodedField[]  readEncodedFields(int size) throws IOException {
                _EncodedField[] ret = new _EncodedField[size];
                for (int i = 0; i < size; i++) {
                    ret[i] = new _EncodedField();
                    ret[i].read();
                }

                return ret;
            }

            _EncodedMethod[]  readEncodedMethods(int size) throws IOException {
                _EncodedMethod[] ret = new _EncodedMethod[size];
                for (int i = 0; i < size; i++) {
                    ret[i] = new _EncodedMethod();
                    ret[i].read();
                }

                return ret;
            }
        }

        public void read() throws IOException {
            classIdx = readUInt();
            accessFlags = readUInt();
            superclassIdx = readUInt();
            interfacesOff = readUInt();
            sourceFileIdx = readUInt();
            annotationsOff = readUInt();
            classDataOff = readUInt();
            staticValuesOff = readUInt();

            raf.seek(classDataOff);
        }

        public ClassDefItem toClassDefItem() throws IOException {
            return new ClassDefItem(
                    new CstType(Type.intern(
                                        addSuffix(typeIdItems[classIdx].getString()))),
                    (int) accessFlags,
                    new CstType(Type.intern(typeIdItems[superclassIdx].getString())),
                    StdTypeList.EMPTY, // TODO: fill list
                    new CstString(getString(sourceFileIdx)));
        }
    }

    private <T extends Readable> T[] readArray(int size, Class<T> klass) throws IOException {
        Object[] ret = new Object[size];
        for (int i = 0; i < size; i ++) {
            try {
                ret[i] = klass.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            ((T) ret[i]).read();
        }
        return (T[]) ret;
    }

    class StringIdItem implements Readable {
        long stringDataOff;

        public void read() throws IOException {
            stringDataOff = readUInt();
        }

        public String getString() throws IOException {
            raf.seek(stringDataOff);
            int len = readULeb128();
            char[] data = new char[len];
            return Mutf8.decode(new MyByteInput(raf), data);
        }
    }

    int readUInt() throws IOException {
        int it = raf.readInt();
        it = Integer.reverseBytes(it);
        return it;
    }

    int readULeb128() throws IOException {
        return Leb128.readUnsignedLeb128(new MyByteInput(raf));
    }

    static class MyByteInput implements ByteInput {
        RandomAccessFile raf;
        public MyByteInput(RandomAccessFile raf) {
            this.raf = raf;
        }

        @Override
        public byte readByte() {
            try {
                return raf.readByte();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public interface Readable {
        public void read() throws IOException;
    }
}
