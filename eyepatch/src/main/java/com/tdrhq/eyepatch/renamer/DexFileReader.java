// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.renamer;

import com.android.dex.Leb128;
import com.android.dex.Mutf8;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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
        @F(idx=1, uleb=true) int fieldIdxDiff;
        @F(idx=2, uleb=true) int accessFlags;
    }

    class _EncodedMethod extends Streamable {
        @F(idx=1, uleb=true) int methodIdxDiff;
        @F(idx=2, uleb=true) int accessFlags;
        @F(idx=3, uleb=true) int codeOff;

        // substructures
        _CodeItem codeItem;

        public void readImpl() throws IOException {
            readObject();

            long mark = raf.getFilePointer();
            raf.seek(codeOff);
            codeItem = new _CodeItem();
            codeItem.read();
            raf.seek(mark);
        }
    }

    class _CodeItem extends Streamable {
        @F(idx=1) short registersSize;
        @F(idx=2) short insSize;
        @F(idx=3) short outsSize;
        @F(idx=4) short triesSize;
        @F(idx=5) int debugInfoOff;
        @F(idx=6) int insnsSize;
        @F(idx=7, sizeIdx=6) short[] insns;
        @F(idx=8) short padding;

        _TryItem[] tryItems;
        _EncodedCatchHandlerList encodedCatchHandlerList = null;

        public void readImpl() throws IOException {
            readObject();
            padding = readUShort();
            tryItems = readArray(triesSize, _TryItem.class);

            if (triesSize != 0) {
                encodedCatchHandlerList = new _EncodedCatchHandlerList();
                encodedCatchHandlerList.read();
            }
        }
    }

    class _TryItem extends Streamable {
        @F(idx=1) int startAddr;
        @F(idx=2) short insnCount;
        @F(idx=3) short handlerOff;
    }

    class _EncodedCatchHandlerList extends Streamable {
        @F(idx=1, uleb=true) int size;
        @F(idx=2, sizeIdx=1) _EncodedCatchHandler[] list;
    }

    class _EncodedCatchHandler extends Streamable {
        long size;
        _EncodedTypeAddrPair[] handlers;
        long catchAllAddr;

        @Override
        public void readImpl() throws IOException {
            size = readSLeb128();

            // handle negative numbers here.
            if (size != 0) {

                throw new RuntimeException("unexpected: " + size);
            }

            handlers = readArray(Math.abs((int) size), _EncodedTypeAddrPair.class);
            catchAllAddr = readULeb128();
        }

    }

    class _EncodedTypeAddrPair extends Streamable {
        @F(idx=1, uleb=true) long typeIdx;
        @F(idx=1, uleb=true) long addr;
    }

    class _ClassDataItem extends Streamable {
        @F(idx=1, uleb=true) int staticFieldsSize;
        @F(idx=2, uleb=true) int instanceFieldsSize;
        @F(idx=3, uleb=true) int directMethodsSize;
        @F(idx=4, uleb=true) int virtualMethodsSize;

        @F(idx=5, sizeIdx=1) _EncodedField[] staticFields;
        @F(idx=6, sizeIdx=2) _EncodedField[] instanceFields;
        @F(idx=7, sizeIdx=3) _EncodedMethod[] directMethods;
        @F(idx=8, sizeIdx=4) _EncodedMethod[] virtualMethods;
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
        @F(idx=1) int stringDataOff;

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

        void readObject() throws IOException {
            Class klass = this.getClass();
            if (klass == Streamable.class) {
                throw new RuntimeException("unexpected");
            }
            List<Field> fields = AnnotationUtil.getAnnotatedFields(klass);
            for (Field f : fields) {
                try {
                    if (f.getType() == int.class) {
                        if (f.getAnnotation(F.class).uleb()) {
                            f.set(this, readULeb128());
                        } else {
                            f.set(this, readUInt());
                        }
                    } else if (f.getType() == short.class) {
                        f.set(this, readUShort());
                    } else if (f.getType() == byte[].class) {
                        byte[] arr = (byte[]) f.get(this);
                        if (arr == null) {
                            throw new NullPointerException();
                        }
                        raf.read(arr);
                    } else if (f.getType() == short[].class) {
                        int size = AnnotationUtil.getSizeFromSizeIdx(this, f);
                        f.set(this, readShortArray(size));
                    } else if (f.getType().isArray()) {
                        Class type = f.getType();
                        Class<? extends Streamable> componentType =
                                (Class<? extends Streamable>) type.getComponentType();
                        int size = AnnotationUtil.getSizeFromSizeIdx(this, f);
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

    private short[] readShortArray(int size) throws IOException {
        short[] ret = new short[size];
        for (int i = 0; i < size; i++) {
            ret[i] = readUShort();
        }
        return ret;
    }

    class _FieldIdItem extends Streamable {
        @F(idx=1) short classIdx;
        @F(idx=2) short typeIdx;
        @F(idx=3) int nameIdx;
    }

    class _MethodIdItem extends Streamable {
        @F(idx=1) short classIdx;
        @F(idx=2) short protoIdx;
        @F(idx=3) int nameIdx;
    }

    class _ProtoIdItem extends Streamable {
        @F(idx=1) int shortyIdx;
        @F(idx=2) int returnTypeIdx;
        @F(idx=3) int parametersOff;
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
