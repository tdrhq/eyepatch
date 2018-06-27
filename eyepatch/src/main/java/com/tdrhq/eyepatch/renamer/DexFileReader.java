// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.renamer;

import android.util.Log;
import com.android.dex.Mutf8;
import com.android.dx.dex.DexOptions;
import com.android.dx.dex.file.ClassDefItem;
import com.android.dx.dex.file.DexFile;
import com.android.dx.dex.file.ItemType;
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
    private RandomAccessFile raf;
    _ClassDefItem[] classDefItems = null;
    _TypeIdItem[] typeIdItems = null;
    _FieldIdItem[] fieldIdItems = null;
    _MethodIdItem[] methodIdItems = null;
    _ProtoIdItem[] protoIdItems = null;
    _MapList mapList = null;
    _DebugInfoItem[] debugInfoItems;
    _StringDataItem[] stringDataItems;
    _ClassDataItem[] classDataItems;
    _CodeItem[] codeItems;

    private DexFile dexFile;
    public void read() throws IOException {

        dexFile = new DexFile(new DexOptions());
        raf = new RandomAccessFile(file, "r");
        headerItem = new HeaderItem(this);
        raf.seek(0);
        headerItem.read(raf);

        mapList = new _MapList(this);
        raf.seek(headerItem.mapOff);
        mapList.read(raf);

        raf.seek(headerItem.stringIdsOff);
        stringIdItems = readArray(
                (int) headerItem.stringIdsSize,
                StringIdItem.class,
                this,
                raf);
        readStringDataItems();

        raf.seek(headerItem.typeIdsOff);
        typeIdItems = readArray(headerItem.typeIdsSize, _TypeIdItem.class, this, raf);

        raf.seek(headerItem.classDefsOff);
        classDefItems = readArray((int) headerItem.classDefsSize, _ClassDefItem.class, this, raf);

        readClassDataItems();

        raf.seek(headerItem.fieldIdsOff);
        fieldIdItems = readArray(headerItem.fieldIdsSize, _FieldIdItem.class, this, raf);

        raf.seek(headerItem.methodIdsOff);
        methodIdItems = readArray(headerItem.methodIdsSize, _MethodIdItem.class, this, raf);

        raf.seek(headerItem.protoIdsOff);
        protoIdItems = readArray(headerItem.protoIdsSize, _ProtoIdItem.class, this, raf);

        readDebugInfoItems();
        readTypeList();
        readAnnotationSetRefList();
        readAnnotationSetItem();
        readEncodedArrayItems();
        readCodeItems();
    }

    private void readDebugInfoItems() throws IOException {
        _MapItem item = getMapItem(ItemType.TYPE_DEBUG_INFO_ITEM);
        if (item == null) {
            return;
        }
        raf.seek(item.offset);
        debugInfoItems = readArray(item.size, _DebugInfoItem.class, this, raf);
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

    private void readCodeItems() throws IOException {
        _MapItem item = getMapItem(ItemType.TYPE_CODE_ITEM);
        if (item == null) {
            return;
        }

        raf.seek(item.offset);
        codeItems = readArray(item.size, _CodeItem.class, this, raf);
    }

    private void readStringDataItems() throws IOException {
        _MapItem item = getMapItem(ItemType.TYPE_STRING_DATA_ITEM);
        if (item == null) {
            throw new RuntimeException("could not find data items");
        }

        raf.seek(item.offset);
        stringDataItems = readArray(item.size, _StringDataItem.class, this, raf);
    }

    private void readClassDataItems() throws IOException {
        _MapItem item = getMapItem(ItemType.TYPE_CLASS_DATA_ITEM);
        if (item == null) {
            throw new RuntimeException("could not find data items");
        }

        raf.seek(item.offset);
        classDataItems = readArray(item.size, _ClassDataItem.class, this, raf);
    }

    static class _StringDataItem extends Streamable {
        int size;
        String decoded;

        @Override
        public boolean isAligned() {
            return false;
        }

        public _StringDataItem(DexFileReader dexFileReader) {
            super(dexFileReader);
        }

        @Override
        public void readImpl(RandomAccessFile raf) throws IOException {
            size = RafUtil.readULeb128(raf);
            char[] data = new char[size];
            decoded = Mutf8.decode(new MyByteInput(raf), data);
        }

        @Override
        public void writeImpl(RandomAccessFile raf) throws IOException {
            RafUtil.writeULeb128(raf, size);
            byte[] output = Mutf8.encode(decoded);
            raf.write(output, 0, output.length);

            byte zero = '\0';
            raf.write(zero);
        }
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
        RandomAccessFile raf = new RandomAccessFile(output, "rw");
        raf.seek(0);
        headerItem.write(raf);

        for (_MapItem item : mapList.list) {
            switch(item.getItemType()) {
            case TYPE_HEADER_ITEM:
                continue;
            case TYPE_MAP_LIST:
                mapList.write(raf);
                break;
            case TYPE_STRING_ID_ITEM:
                writeArray(stringIdItems, raf);
                break;
            case TYPE_TYPE_ID_ITEM:
                writeArray(typeIdItems, raf);
                break;
            case TYPE_PROTO_ID_ITEM:
                writeArray(protoIdItems, raf);
                break;
            case TYPE_FIELD_ID_ITEM:
                writeArray(fieldIdItems, raf);
                break;
            case TYPE_METHOD_ID_ITEM:
                writeArray(methodIdItems, raf);
                break;
            case TYPE_CLASS_DEF_ITEM:
                writeArray(classDefItems, raf);
                break;
            case TYPE_CODE_ITEM:
                writeArray(codeItems, raf);
                break;
            case TYPE_STRING_DATA_ITEM:
                writeArray(stringDataItems, raf);
                break;
            case TYPE_CLASS_DATA_ITEM:
                writeArray(classDataItems, raf);
                break;
            default:
                throw new RuntimeException("unsupported type: " + item.getItemType().toString());
            }
        }
        Log.i("DexFileReader", "Before closing position is: " + raf.getFilePointer());
        raf.close();
    }

    String getString(long idx) throws IOException {
        long off = stringIdItems[(int) idx].stringDataOff;
        for (_StringDataItem  dataItem : stringDataItems) {
            if (dataItem.getOrigOffset() == off) {
                return dataItem.decoded;
            }
        }

        throw new RuntimeException("could not find data for this string");
    }

    static class HeaderItem extends Streamable {
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
        @F(idx=22) int dataSize;
        @F(idx=23) int dataOff;

        public HeaderItem(DexFileReader dexFileReader) {
            super(dexFileReader);
        }

        @Override
        public void readImpl(RandomAccessFile raf) throws IOException {
            super.readImpl(raf);
            Log.i("DexFileReader", "checksum is: " + checksum + " " + String.format("%8x", checksum));
        }
    }

    _MapItem getMapItem(ItemType itemType) {
        for (int i = 0; i < mapList.size; i++) {
            if (mapList.list[i].type == itemType.getMapValue()) {
                return mapList.list[i];
            }
        }
        return null;
    }

    static class _MapList extends Streamable {
        @F(idx=1) int size;
        @F(idx=2, sizeIdx=1) _MapItem[] list;

        public _MapList(DexFileReader dexFileReader) {
            super(dexFileReader);
        }
    }

    static class _MapItem extends Streamable {
        @F(idx=1) short type;
        @F(idx=2) short unused;
        @F(idx=3) int size;
        @F(idx=4) int offset;

        public _MapItem(DexFileReader dexFileReader) {
            super(dexFileReader);
        }

        public ItemType getItemType() {
            for (ItemType _type : ItemType.values()) {
                if (_type.getMapValue() == type) {
                    return _type;
                }
            }
            throw new RuntimeException("unknown type");
        }
    }

    static class _TypeIdItem extends Streamable {
        @F(idx=1) int descriptorIdx; // a  pointer to string_ids

        public _TypeIdItem(DexFileReader dexFileReader) {
            super(dexFileReader);
        }

        public String getString() throws IOException{
            return dexFileReader.getString(descriptorIdx);
        }
    }

    static class _EncodedField extends Streamable {
        @F(idx=1, uleb=true) int fieldIdxDiff;
        @F(idx=2, uleb=true) int accessFlags;

        @Override
        public boolean isAligned() {
            return false;
        }

        public _EncodedField(DexFileReader dexFileReader) {
            super(dexFileReader);
        }
    }

    static class _EncodedMethod extends Streamable {
        @F(idx=1, uleb=true) int methodIdxDiff;
        @F(idx=2, uleb=true) int accessFlags;
        @F(idx=3, uleb=true) int codeOff;

        @Override
        public boolean isAligned() {
            return false;
        }

        public _EncodedMethod(DexFileReader dexFileReader) {
            super(dexFileReader);
        }

        @Override
        public void readImpl(RandomAccessFile raf) throws IOException {
            super.readImpl(raf);
            Log.i("DexFileReader", "Read encoded method as: " + methodIdxDiff + " " + accessFlags + " " + codeOff);
        }
    }

    public _CodeItem getCodeItem(_EncodedMethod method) {
        for (_CodeItem codeItem : codeItems) {
            if (codeItem.getOrigOffset() == method.codeOff) {
                return codeItem;
            }
        }

        throw new RuntimeException("could not find codeItem");
    }

    static class _CodeItem extends Streamable {
        @F(idx=1) short registersSize;
        @F(idx=2) short insSize;
        @F(idx=3) short outsSize;
        @F(idx=4) short triesSize;
        @F(idx=5) int debugInfoOff;
        @F(idx=6) int insnsSize;
        @F(idx=7, sizeIdx=6) short[] insns;

        // these are conditional
        short padding = 0;
        _TryItem[] tryItems;
        _EncodedCatchHandlerList encodedCatchHandlerList = null;

        public _CodeItem(DexFileReader dexFileReader) {
            super(dexFileReader);
        }

        @Override
        public void readImpl(RandomAccessFile raf) throws IOException {
            readObject(raf);

            if (insnsSize % 2 != 0 && triesSize > 0) {
                padding = RafUtil.readUShort(raf);
            }

            if (triesSize > 0) {
                tryItems = readArray(triesSize, _TryItem.class, dexFileReader, raf);
                encodedCatchHandlerList = new _EncodedCatchHandlerList(dexFileReader);
                encodedCatchHandlerList.read(raf);
            }
        }

        @Override
        public void writeImpl(RandomAccessFile raf) throws IOException {
            writeObject(raf);

            if (insnsSize % 2 != 0) {

                // acc. to the doc this should only be written if
                // triesSize >0, but it should be safe to always write
                // it, and dexmaker seems to be doing that too
                RafUtil.writeUShort(raf, padding);
            }

            if (triesSize > 0) {
                writeArray(tryItems, raf);
                encodedCatchHandlerList.write(raf);
            }
        }
    }

    static class _TryItem extends Streamable {
        @F(idx=1) int startAddr;
        @F(idx=2) short insnCount;
        @F(idx=3) short handlerOff;
        public _TryItem(DexFileReader dexFileReader) {
            super(dexFileReader);
        }
    }

    static class _EncodedCatchHandlerList extends Streamable {
        @F(idx=1, uleb=true) int size;
        @F(idx=2, sizeIdx=1) _EncodedCatchHandler[] list;

        public _EncodedCatchHandlerList(DexFileReader dexFileReader) {
            super(dexFileReader);
        }
    }

    static class _EncodedCatchHandler extends Streamable {
        long size;
        _EncodedTypeAddrPair[] handlers;
        long catchAllAddr;

        public _EncodedCatchHandler(DexFileReader dexFileReader) {
            super(dexFileReader);
        }

        @Override
        public void readImpl(RandomAccessFile raf) throws IOException {
            size = RafUtil.readSLeb128(raf);

            // handle negative numbers here.
            if (size != 0) {

                throw new RuntimeException("unexpected: " + size);
            }

            handlers = readArray(Math.abs((int) size), _EncodedTypeAddrPair.class, this, raf);
            catchAllAddr = RafUtil.readULeb128(raf);
        }

    }

    static class _EncodedTypeAddrPair extends Streamable {
        @F(idx=1, uleb=true) long typeIdx;
        @F(idx=1, uleb=true) long addr;

        public _EncodedTypeAddrPair(DexFileReader dexFileReader) {
            super(dexFileReader);
        }
    }

    static class _ClassDataItem extends Streamable {
        @F(idx=1, uleb=true) int staticFieldsSize;
        @F(idx=2, uleb=true) int instanceFieldsSize;
        @F(idx=3, uleb=true) int directMethodsSize;
        @F(idx=4, uleb=true) int virtualMethodsSize;

        @F(idx=5, sizeIdx=1) _EncodedField[] staticFields;
        @F(idx=6, sizeIdx=2) _EncodedField[] instanceFields;
        @F(idx=7, sizeIdx=3) _EncodedMethod[] directMethods;
        @F(idx=8, sizeIdx=4) _EncodedMethod[] virtualMethods;

        @Override
        public boolean isAligned() {
            return false;
        }

        public _ClassDataItem(DexFileReader dexFileReader) {
            super(dexFileReader);
        }
    }

    public _ClassDataItem getClassDataItem(_ClassDefItem classDefItem) {
        for (_ClassDataItem dataItem : classDataItems) {
            if (dataItem.getOrigOffset() == classDefItem.classDataOff) {
                return dataItem;
            }
        }

        throw new RuntimeException("could not find data item");
    }

    static class _ClassDefItem extends Streamable {
        @F(idx=1) int classIdx;
        @F(idx=2) int accessFlags;
        @F(idx=3) int superclassIdx;
        @F(idx=4) int interfacesOff;
        @F(idx=5) int sourceFileIdx;
        @F(idx=6) int annotationsOff;
        @F(idx=7) int classDataOff;
        @F(idx=8) int staticValuesOff;

        public _ClassDefItem(DexFileReader dexFileReader) {
            super(dexFileReader);
        }


        @Override
        public void readImpl(RandomAccessFile raf) throws IOException {
            readObject(raf);
        }

    }

    static <T extends Streamable> T[] readArray(int size, Class<T> klass, Object parent, RandomAccessFile raf) throws IOException {
        T[] ret = (T[]) Array.newInstance(klass, size);
        for (int i = 0; i < size; i ++) {
            try {
                try {
                    Constructor cons = klass.getDeclaredConstructor();
                    ret[i] = (T) cons.newInstance();
                } catch (NoSuchMethodException e) {
                    if (parent == null) {
                        throw new RuntimeException("need parent to create this instance");
                    }
                    Constructor cons = klass.getDeclaredConstructor(parent.getClass());
                    ret[i] = (T) cons.newInstance(parent);
                }
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            ret[i].read(raf);
        }
        return ret;
    }

    static <T extends Streamable> void writeArray(T[] ts, RandomAccessFile raf) throws IOException  {
        for (int i = 0; i < ts.length; i++) {
            ts[i].write(raf);
        }
    }

    static class StringIdItem extends Streamable {
        @F(idx=1) int stringDataOff;

        public StringIdItem(DexFileReader reader) {
            super(reader);
        }
    }

    static class _FieldIdItem extends Streamable {
        @F(idx=1) short classIdx;
        @F(idx=2) short typeIdx;
        @F(idx=3) int nameIdx;

        public _FieldIdItem(DexFileReader dexFileReader) {
            super(dexFileReader);
        }
    }

    static class _MethodIdItem extends Streamable {
        @F(idx=1) short classIdx;
        @F(idx=2) short protoIdx;
        @F(idx=3) int nameIdx;

        public _MethodIdItem(DexFileReader dexFileReader) {
            super(dexFileReader);
        }
    }

    static class _ProtoIdItem extends Streamable {
        @F(idx=1) int shortyIdx;
        @F(idx=2) int returnTypeIdx;
        @F(idx=3) int parametersOff;

        public _ProtoIdItem(DexFileReader dexFileReader) {
            super(dexFileReader);
        }
    }

    static class _DebugInfoItem extends Streamable {
        int lineStart;
        int parametersSize;
        int[] parameterNames;

        public _DebugInfoItem(DexFileReader dexFileReader) {
            super(dexFileReader);
        }

        @Override
        public boolean isAligned() {
            return false;
        }

        @Override
        public void readImpl(RandomAccessFile raf) throws IOException {
            lineStart = RafUtil.readULeb128(raf);
            parametersSize = RafUtil.readULeb128(raf);
            parameterNames = new int[parametersSize];
            for (int i = 0; i < parametersSize; i++) {
                parameterNames[i] = RafUtil.readULeb128(raf);
            }
        }
    }

    interface NameProvider {
        public String rename(String input);
    }
}
