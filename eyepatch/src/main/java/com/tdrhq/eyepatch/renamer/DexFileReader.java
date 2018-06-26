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
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Reads a {@code DexFile} from the given file.
 *
 * This lets us modifiy the structures of the DexFile in order to
 * rewrite it as something else.
 */
public class DexFileReader {
    private File file;
    private NameProvider nameProvider;

    public DexFileReader(File file, NameProvider nameProvider) {
        this.file = file;
        this.nameProvider = nameProvider;
    }

    HeaderItem headerItem = null;
    StringIdItem[] stringIdItems = null;
    RandomAccessFile raf;
    _ClassDefItem[] classDefItems = null;
    _TypeIdItem[] typeIdItems = null;
    _FieldIdItem[] fieldIdItems = null;
    _MethodIdItem[] methodIdItems = null;
    _ProtoIdItem[] protoIdItems = null;

    public DexFile read() throws IOException {

        DexFile dexFile = new DexFile(new DexOptions());
        raf = new RandomAccessFile(file, "r");
        headerItem = new HeaderItem();
        headerItem.read();

        raf.seek(headerItem.stringIdsOff);
        stringIdItems = readArray(
                (int) headerItem.stringIdsSize,
                StringIdItem.class);

        raf.seek(headerItem.typeIdsOff);
        typeIdItems = readArray(headerItem.typeIdsSize, _TypeIdItem.class);

        raf.seek(headerItem.classDefsOff);
        classDefItems = readArray((int) headerItem.classDefsSize, _ClassDefItem.class);

        for (int i = 0; i < headerItem.classDefsSize; i++) {
            dexFile.add(classDefItems[i].toClassDefItem());
        }

        raf.seek(headerItem.fieldIdsOff);
        fieldIdItems = readArray(headerItem.fieldIdsSize, _FieldIdItem.class);

        raf.seek(headerItem.methodIdsOff);
        methodIdItems = readArray(headerItem.methodIdsSize, _MethodIdItem.class);

        raf.seek(headerItem.protoIdsOff);
        protoIdItems = readArray(headerItem.protoIdsSize, _ProtoIdItem.class);

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

        // substructures
        _CodeItem codeItem;

        public void read() throws IOException {
            methodIdxDiff = readULeb128();
            accessFlags = readULeb128();
            codeOff = readULeb128();

            long mark = raf.getFilePointer();
            raf.seek(codeOff);
            codeItem = new _CodeItem();
            codeItem.read();
            raf.seek(mark);
        }
    }

    class _CodeItem implements Readable {
        short registersSize;
        short insSize;
        short outsSize;
        short triesSize;
        int debugInfoOff;
        int insnsSize;
        short[] insns;
        short padding;

        _TryItem[] tryItems;
        // handlers

        public void read() throws IOException {
            registersSize = readUShort();
            insSize = readUShort();
            outsSize = readUShort();
            triesSize = readUShort();
            debugInfoOff = readUInt();
            insnsSize = readUInt();
            insns = new short[insnsSize];
            for (int i = 0 ; i < insnsSize; i ++) {
                insns[i] = readUShort();
            }
            padding = readUShort();
            tryItems = readArray(triesSize, _TryItem.class);
        }
    }

    class _TryItem implements Readable {
        int startAddr;
        short insnCount;
        short handlerOff;

        public void read() throws IOException {
            startAddr = readUInt();
            insnCount = readUShort();
            handlerOff = readUShort();
        }
    }

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
            return readArray(size, _EncodedField.class);
        }

        _EncodedMethod[]  readEncodedMethods(int size) throws IOException {
            return readArray(size, _EncodedMethod.class);
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

        // substructurs
        _ClassDataItem classDataItem;


        public void read() throws IOException {
            classIdx = readUInt();
            accessFlags = readUInt();
            superclassIdx = readUInt();
            interfacesOff = readUInt();
            sourceFileIdx = readUInt();
            annotationsOff = readUInt();
            classDataOff = readUInt();
            staticValuesOff = readUInt();

            long mark = raf.getFilePointer();
            raf.seek(classDataOff);
            classDataItem = new _ClassDataItem();
            classDataItem.read();
            raf.seek(mark);

        }

        public ClassDefItem toClassDefItem() throws IOException {
            return new ClassDefItem(
                    new CstType(
                            Type.intern(
                                    nameProvider.rename(typeIdItems[classIdx].getString()))),
                    (int) accessFlags,
                    new CstType(Type.intern(typeIdItems[superclassIdx].getString())),
                    StdTypeList.EMPTY, // TODO: fill list
                    new CstString(getString(sourceFileIdx)));
        }
    }

    private <T extends Readable> T[] readArray(int size, Class<T> klass) throws IOException {
        T[] ret = (T[]) Array.newInstance(klass, size);
        for (int i = 0; i < size; i ++) {
            try {
                Constructor cons = klass.getDeclaredConstructor(DexFileReader.class);
                ret[i] = (T) cons.newInstance(this);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            ret[i].read();
        }
        return ret;
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

    short readUShort() throws IOException {
        short it = raf.readShort();
        it = Short.reverseBytes(it);
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

    class _FieldIdItem implements Readable {
        int classIdx;
        int typeIdx;
        int nameIdx;

        public void read() throws IOException {
            classIdx = readUShort();
            typeIdx = readUShort();
            nameIdx = readUInt();
        }
    }

    class _MethodIdItem implements Readable {
        int classIdx;
        int protoIdx;
        int nameIdx;

        public void read() throws IOException {
            classIdx = readUShort();
            protoIdx = readUShort();
            nameIdx = readUInt();
        }
    }

    class _ProtoIdItem implements Readable {
        int shortyIdx;
        int returnTypeIdx;
        int parametersOff;

        public void read() throws IOException {
            shortyIdx = readUInt();
            returnTypeIdx = readUInt();
            parametersOff = readUInt();
        }
    }

    interface NameProvider {
        public String rename(String input);
    }
}
