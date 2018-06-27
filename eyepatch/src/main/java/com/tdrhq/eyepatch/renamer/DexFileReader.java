// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.renamer;

import com.android.dex.Leb128;
import com.android.dex.Mutf8;
import com.android.dex.util.ByteInput;
import com.android.dx.dex.DexOptions;
import com.android.dx.dex.file.ClassDefItem;
import com.android.dx.dex.file.DexFile;
import com.android.dx.dex.file.ItemType;
import com.android.dx.rop.cst.CstString;
import com.android.dx.rop.cst.CstType;
import com.android.dx.rop.type.StdTypeList;
import com.android.dx.rop.type.Type;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;

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
    _MapList mapList = null;
    _DebugInfoItem[] debugInfoItems;

    private DexFile dexFile;
    public void read() throws IOException {

        dexFile = new DexFile(new DexOptions());
        raf = new RandomAccessFile(file, "r");
        headerItem = new HeaderItem();
        raf.seek(0);
        headerItem.read();

        mapList = new _MapList();
        raf.seek(headerItem.mapOff);
        mapList.read();

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

        readDebugInfoItems();
        readTypeList();
        readAnnotationSetRefList();
        readAnnotationSetItem();
        readEncodedArrayItems();
    }

    private void readDebugInfoItems() throws IOException {
        _MapItem item = getMapItem(ItemType.TYPE_DEBUG_INFO_ITEM);
        if (item == null) {
            return;
        }
        raf.seek(item.offset);
        debugInfoItems = readArray(item.size, _DebugInfoItem.class);
    }

    private void readTypeList() throws IOException {
        _MapItem item = getMapItem(ItemType.TYPE_TYPE_LIST);
        if (item == null) {
            return;
        }
        raf.seek(item.offset);
    }

    private void readAnnotationSetRefList() throws IOException {
        _MapItem item = getMapItem(ItemType.TYPE_ANNOTATION_SET_REF_LIST);
        if (item == null) {
            return;
        }

        throw new UnsupportedOperationException();
    }

    private void readAnnotationSetItem() throws IOException {
        _MapItem item = getMapItem(ItemType.TYPE_ANNOTATION_SET_ITEM);
        if (item == null) {
            return;
        }

        throw new UnsupportedOperationException();
    }

    private void readEncodedArrayItems() throws IOException {
        _MapItem item = getMapItem(ItemType.TYPE_ENCODED_ARRAY_ITEM);
        if (item == null) {
            return;
        }

        throw new UnsupportedOperationException();
    }


    public void write(File output) throws IOException {
        FileOutputStream os = new FileOutputStream(output);
        dexFile.writeTo(os, new PrintWriter(System.err), false);
        os.close();
    }

    String getString(long idx) throws IOException {
        return stringIdItems[(int) idx].getString();
    }

    class HeaderItem extends Streamable {
        @F(idx=1) byte[] magic = new byte[8];
        @F(idx=2) int checksum;
        @F(idx=3) byte[] signature = new byte[20];
        @F(idx=4) int fileSize;
        @F(idx=5) int headerSize;
        @F(idx=6) int endianTag;
        @F(idx=7) int linkSize;
        @F(idx=8) int linkOff;
        @F(idx=9) int mapOff;
        @F(idx=10) int stringIdsSize;
        @F(idx=11) int stringIdsOff;
        @F(idx=12) int typeIdsSize;
        @F(idx=13) int typeIdsOff;
        @F(idx=14) int protoIdsSize;
        @F(idx=15) int protoIdsOff;
        @F(idx=16) int fieldIdsSize;
        @F(idx=17) int fieldIdsOff;
        @F(idx=18) int methodIdsSize;
        @F(idx=19) int methodIdsOff;
        @F(idx=20) int classDefsSize;
        @F(idx=21) int classDefsOff;
    }

    _MapItem getMapItem(ItemType itemType) {
        for (int i = 0; i < mapList.size; i++) {
            if (mapList.list[i].type == itemType.getMapValue()) {
                return mapList.list[i];
            }
        }
        return null;
    }

    class _MapList extends Streamable {
        @F(idx=1) int size;
        @F(idx=2, sizeIdx=1) _MapItem[] list;
    }

    class _MapItem extends Streamable {
        @F(idx=1) short type;
        @F(idx=2) short unused;
        @F(idx=3) int size;
        @F(idx=4) int offset;
    }

    class _TypeIdItem extends Streamable {
        int descriptorIdx; // a  pointer to string_ids

        public void readImpl() throws IOException {
            descriptorIdx = readUInt();
        }

        public String getString() throws IOException{
            return DexFileReader.this.getString(descriptorIdx);
        }
    }

    class _EncodedField extends Streamable {
        long fieldIdxDiff;
        long accessFlags;

        public void readImpl() throws IOException {
            fieldIdxDiff = readULeb128();
            accessFlags =readULeb128();
        }
    }

    class _EncodedMethod extends Streamable {
        long methodIdxDiff;
        long accessFlags;
        long codeOff;

        // substructures
        _CodeItem codeItem;

        public void readImpl() throws IOException {
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

    class _CodeItem extends Streamable {
        short registersSize;
        short insSize;
        short outsSize;
        short triesSize;
        int debugInfoOff;
        int insnsSize;
        short[] insns;
        short padding;

        _TryItem[] tryItems;
        _EncodedCatchHandlerList encodedCatchHandlerList = null;

        public void readImpl() throws IOException {
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

            if (triesSize != 0) {
                encodedCatchHandlerList = new _EncodedCatchHandlerList();
                encodedCatchHandlerList.read();
            }
        }
    }

    class _TryItem extends Streamable {
        int startAddr;
        short insnCount;
        short handlerOff;

        public void readImpl() throws IOException {
            startAddr = readUInt();
            insnCount = readUShort();
            handlerOff = readUShort();
        }
    }

    class _EncodedCatchHandlerList {
        long size;
        _EncodedCatchHandler[] list;

        public void read() throws IOException {
            size = readULeb128();
            list = readArray((int) size, _EncodedCatchHandler.class);
        }
    }

    class _EncodedCatchHandler extends Streamable {
        long size;
        _EncodedTypeAddrPair[] handlers;
        long catchAllAddr;

        @Override
        public void readImpl() throws IOException {
            size = readSLeb128();

            if (size != 0) {

                throw new RuntimeException("unexpected: " + size);
            }

            handlers = readArray(Math.abs((int) size), _EncodedTypeAddrPair.class);
            catchAllAddr = readULeb128();
        }

    }

    class _EncodedTypeAddrPair extends Streamable {
        long typeIdx;
        long addr;

        public void readImpl() throws IOException {
            typeIdx = readULeb128();
            addr = readULeb128();
        }
    }

    class _ClassDataItem {
        long staticFieldsSize;
        long instanceFieldsSize;
        long directMethodsSize;
        long virtualMethodsSize;

        _EncodedField[] staticFields;
        _EncodedField[] instanceFields;
        _EncodedMethod[] directMethods;
        _EncodedMethod[] virtualMethods;

        public void read() throws IOException {
            staticFieldsSize = readULeb128();
            instanceFieldsSize = readULeb128();
            directMethodsSize = readULeb128();
            virtualMethodsSize = readULeb128();

            staticFields = readEncodedFields((int) staticFieldsSize);
            instanceFields = readEncodedFields((int) instanceFieldsSize);

            directMethods = readEncodedMethods((int) directMethodsSize);
            virtualMethods = readEncodedMethods((int) virtualMethodsSize);
        }

        _EncodedField[]  readEncodedFields(int size) throws IOException {
            return readArray(size, _EncodedField.class);
        }

        _EncodedMethod[]  readEncodedMethods(int size) throws IOException {
            return readArray(size, _EncodedMethod.class);
        }
    }

    class _ClassDefItem extends Streamable {
        @F(idx=1) int classIdx;
        @F(idx=2) int accessFlags;
        @F(idx=3) int superclassIdx;
        @F(idx=4) int interfacesOff;
        @F(idx=5) int sourceFileIdx;
        @F(idx=6) int annotationsOff;
        @F(idx=7) int classDataOff;
        @F(idx=8) int staticValuesOff;

        // substructurs
        _ClassDataItem classDataItem;


        public void readImpl() throws IOException {
            readObject();

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

    private <T extends Streamable> T[] readArray(int size, Class<T> klass) throws IOException {
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

    class StringIdItem extends Streamable {
        long stringDataOff;

        public void readImpl() throws IOException {
            stringDataOff = readUInt();
        }

        public String getString() throws IOException {
            raf.seek(stringDataOff);
            long len = readULeb128();
            char[] data = new char[(int) len];
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

    int readSLeb128() throws IOException {
        return Leb128.readSignedLeb128(new MyByteInput(raf));
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

    public abstract class Streamable {
        private long origOffset = -1;
        private long writeOffset = -1;

        public void read() throws IOException {
            origOffset = raf.getFilePointer();
            readImpl();
        }

        public void write(RandomAccessFile output) throws IOException {
            writeOffset = output.getFilePointer();
            writeImpl();
        }

        private int getIndex(Field field) {
            F f = field.getAnnotation(F.class);
            if (f == null) {
                return -1;
            }
            return f.idx();
        }

        void readObject() throws IOException {
            Class klass = this.getClass();
            if (klass == Streamable.class) {
                throw new RuntimeException("unexpected");
            }
            Field[] fields = klass.getDeclaredFields();
            Arrays.sort(fields, new Comparator<Field>() {
                @Override
                public int compare(Field field, Field t1) {
                    return getIndex(field) - getIndex(t1);
                }
            });

            for (Field f : fields) {
                if (f.getAnnotation(F.class) == null) {
                    continue;
                }
                try {
                    if (f.getType() == int.class) {
                        f.set(this, readUInt());
                    } else if (f.getType() == short.class) {
                        f.set(this, readUShort());
                    } else if (f.getType() == byte[].class) {
                        byte[] arr = (byte[]) f.get(this);
                        if (arr == null) {
                            throw new NullPointerException();
                        }
                        raf.read(arr);
                    } else if (f.getType().isArray()) {
                        Class type = f.getType();
                        Class<? extends Streamable> componentType =
                                (Class<? extends Streamable>) type.getComponentType();
                        int size = -1;
                        int sizeIdx = f.getAnnotation(F.class).sizeIdx();
                        for (Field sizeField : fields) {
                            if (getIndex(sizeField) == sizeIdx) {
                                size = (int) sizeField.get(this);
                            }
                        }
                        if (size == -1) {
                            throw new RuntimeException("could not find index: " + sizeIdx);
                        }
                        f.set(this, readArray(size, componentType));
                    }
                    else {
                        throw new UnsupportedOperationException();
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        protected void readImpl() throws IOException {
            readObject();
        }

        public void writeImpl() throws IOException {
            throw new UnsupportedOperationException();
        }
    }

    class _FieldIdItem extends Streamable {
        int classIdx;
        int typeIdx;
        int nameIdx;

        public void readImpl() throws IOException {
            classIdx = readUShort();
            typeIdx = readUShort();
            nameIdx = readUInt();
        }
    }

    class _MethodIdItem extends Streamable {
        int classIdx;
        int protoIdx;
        int nameIdx;

        public void readImpl() throws IOException {
            classIdx = readUShort();
            protoIdx = readUShort();
            nameIdx = readUInt();
        }
    }

    class _ProtoIdItem extends Streamable {
        int shortyIdx;
        int returnTypeIdx;
        int parametersOff;

        public void readImpl() throws IOException {
            shortyIdx = readUInt();
            returnTypeIdx = readUInt();
            parametersOff = readUInt();
        }
    }

    class _DebugInfoItem extends Streamable {
        int lineStart;
        int parametersSize;
        int[] parameterNames;

        public void readImpl() throws IOException {
            lineStart = readULeb128();
            parametersSize = readULeb128();
            parameterNames = new int[parametersSize];
            for (int i = 0; i < parametersSize; i++) {
                parameterNames[i] = readULeb128();
            }
        }
    }

    interface NameProvider {
        public String rename(String input);
    }
}
