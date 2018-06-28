// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.renamer;

import android.util.Log;
import com.android.dex.Mutf8;
import com.android.dx.dex.DexOptions;
import com.android.dx.dex.file.DexFile;
import com.android.dx.dex.file.ItemType;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Adler32;

/**
 * Reads a {@code DexFile} from the given file.
 *
 * This lets us modifiy the structures of the DexFile in order to
 * rewrite it as something else.
 */
public class DexFileReader implements CodeItemRewriter.StringIdProvider {
    private File file;
    private NameProvider nameProvider;
    private int insertedOffsets = -100;

    Map<Integer, Integer> offsetMap = new HashMap<>();

    public DexFileReader(File file, NameProvider nameProvider) {
        this.file = file;
        this.nameProvider = nameProvider;
    }

    HeaderItem headerItem = null;
    MapList mapList = null;

    StringIdItem[] stringIdItems = null;
    private RandomAccessFile raf;
    ClassDefItem[] classDefItems = null;
    TypeIdItem[] typeIdItems = null;
    FieldIdItem[] fieldIdItems = null;
    MethodIdItem[] methodIdItems = null;
    ProtoIdItem[] protoIdItems = null;
    DebugInfoItem[] debugInfoItems;
    _StringDataItem[] stringDataItems;
    ClassDataItem[] classDataItems;
    CodeItem[] codeItems;

    private DexFile dexFile;
    public void read() throws IOException {

        dexFile = new DexFile(new DexOptions());
        raf = new RandomAccessFile(file, "r");
        headerItem = new HeaderItem(this);
        raf.seek(0);
        headerItem.read(raf);

        mapList = new MapList(this);
        raf.seek(headerItem.mapOff);
        mapList.read(raf);

        for (MapItem item : mapList.list) {
            raf.seek(item.offset);
            switch (item.getItemType()) {
            case TYPE_HEADER_ITEM:
                break;
            case TYPE_STRING_ID_ITEM:
                stringIdItems = readArray(
                        (int) headerItem.stringIdsSize,
                        StringIdItem.class,
                        this,
                        raf);

                for (int i = 0; i < stringIdItems.length; i++) {
                    stringIdItems[i].originalIndex = i;
                }
                break;
            case TYPE_TYPE_ID_ITEM:
                typeIdItems = readArray(headerItem.typeIdsSize, TypeIdItem.class, this, raf);
                break;

            case TYPE_PROTO_ID_ITEM:
                protoIdItems = readArray(headerItem.protoIdsSize, ProtoIdItem.class, this, raf);
                break;
            case TYPE_METHOD_ID_ITEM:
                methodIdItems = readArray(headerItem.methodIdsSize, MethodIdItem.class, this, raf);
                break;
            case TYPE_CLASS_DEF_ITEM:
                classDefItems = readArray((int) headerItem.classDefsSize, ClassDefItem.class, this, raf);
                break;
            case TYPE_CODE_ITEM:
                codeItems = readArray(item.size, CodeItem.class, this, raf);
                break;

            case TYPE_STRING_DATA_ITEM:
                stringDataItems = readArray(item.size, _StringDataItem.class, this, raf);
                break;

            case TYPE_CLASS_DATA_ITEM:
                classDataItems = readArray(item.size, ClassDataItem.class, this, raf);
                break;
            case TYPE_MAP_LIST:
                break;

            default:
                throw new UnsupportedOperationException("unexpected type: " + item.getItemType());
            }
        }


        readDebugInfoItems();
        readTypeList();
        readAnnotationSetRefList();
        readAnnotationSetItem();
        readEncodedArrayItems();
    }

    @Override
    public int getUpdatedStringIndex(int originalIndex) {
        for (int i = 0; i < stringIdItems.length; i++) {
            if (originalIndex == stringIdItems[i].originalIndex) {
                return i;
            }
        }

        throw new RuntimeException("no such id");
    }

    public void addString(String val) {
        stringDataItems = Arrays.copyOf(stringDataItems, stringDataItems.length + 1);

        _StringDataItem newDataItem = new _StringDataItem(this);
        newDataItem.decoded = val;
        stringDataItems[stringDataItems.length - 1] = newDataItem;

        int newOffset = (insertedOffsets --);
        newDataItem.setOrigOffset(newOffset);

        // now to figure out where to insert the idItem.
        int insertPos = 0;
        for (; insertPos < stringIdItems.length; insertPos++) {
            if (compareStrings(
                        val,
                        getString(insertPos))) {
                break;
            }

        }

        stringIdItems = Arrays.copyOf(stringIdItems, stringIdItems.length + 1);
        StringIdItem newItem =  new StringIdItem(this);
        stringIdItems[stringIdItems.length - 1] = newItem;
        for (int i = stringIdItems.length - 1; i > insertPos; i--) {
            stringIdItems[i].stringDataOff = stringIdItems[i - 1].stringDataOff;
            stringIdItems[i].originalIndex = stringIdItems[i - 1].originalIndex;
        }

        stringIdItems[insertPos].stringDataOff = (int) newDataItem.getOrigOffset();
        stringIdItems[insertPos].originalIndex = -1;

        headerItem.stringIdsSize ++;

        for (MapItem item : mapList.list) {
            switch (item.getItemType()) {
            case TYPE_STRING_ID_ITEM:
                item.size ++;
                break;
            case TYPE_STRING_DATA_ITEM:
                item.size++;
                break;
            }
        }
    }

    static boolean compareStrings(String one, String two) {
        return one.compareTo(two) < 0;
    }

    private void readDebugInfoItems() throws IOException {
        MapItem item = getMapItem(ItemType.TYPE_DEBUG_INFO_ITEM);
        if (item == null) {
            return;
        }
        raf.seek(item.offset);
        debugInfoItems = readArray(item.size, DebugInfoItem.class, this, raf);
    }

    private void readTypeList() throws IOException {
        MapItem item = getMapItem(ItemType.TYPE_TYPE_LIST);
        if (item == null) {
            return;
        }
        raf.seek(item.offset);
    }

    private void readAnnotationSetRefList() throws IOException {
        MapItem item = getMapItem(ItemType.TYPE_ANNOTATION_SET_REF_LIST);
        if (item == null) {
            return;
        }

        throw new UnsupportedOperationException();
    }

    static class _StringDataItem extends Streamable {
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
            int size = RafUtil.readULeb128(raf);
            char[] data = new char[size];
            decoded = Mutf8.decode(new MyByteInput(raf), data);
        }

        @Override
        public void writeImpl(RandomAccessFile raf) throws IOException {
            RafUtil.writeULeb128(raf, decoded.length());
            byte[] output = Mutf8.encode(decoded);
            raf.write(output, 0, output.length);

            byte zero = '\0';
            raf.write(zero);
        }

        @Override
        public void updateOffsetsImpl() {
            // do nothing
        }
    }

    private void readAnnotationSetItem() throws IOException {
        MapItem item = getMapItem(ItemType.TYPE_ANNOTATION_SET_ITEM);
        if (item == null) {
            return;
        }

        throw new UnsupportedOperationException();
    }

    private void readEncodedArrayItems() throws IOException {
        MapItem item = getMapItem(ItemType.TYPE_ENCODED_ARRAY_ITEM);
        if (item == null) {
            return;
        }

        throw new UnsupportedOperationException();
    }


    public void write(File output) throws IOException {
        updateStringIds();
        RandomAccessFile raf = new RandomAccessFile(output, "rw");
        writeAll(raf);

        headerItem.fileSize = (int) raf.getFilePointer();
        updateOffsets();
        writeAll(raf);

        updateMessageDigest(raf);
        raf.seek(0);
        headerItem.write(raf);
        updateChecksum(raf);
        raf.seek(0);
        headerItem.write(raf);
        Log.i("DexFileReader", "Before closing position is: " + raf.getFilePointer());
        raf.close();
    }

    private void updateStringIds() {
        if (fieldIdItems != null) {
            for (FieldIdItem fieldIdItem : fieldIdItems) {
                fieldIdItem.nameIdx = getUpdatedStringIndex(fieldIdItem.nameIdx);
            }
        }

        if (methodIdItems != null) {
            for (MethodIdItem methodIdItem : methodIdItems) {
                methodIdItem.nameIdx = getUpdatedStringIndex(methodIdItem.nameIdx);
            }
        }

        if (typeIdItems != null) {
            for (TypeIdItem typeIdItem : typeIdItems) {
                typeIdItem.descriptorIdx = getUpdatedStringIndex(typeIdItem.descriptorIdx);
            }
        }

        if (codeItems != null) {
            for (CodeItem codeItem : codeItems) {
                CodeItemRewriter.updateStringIdsInCodeItem(this, codeItem);
            }
        }
    }

    private void writeAll(RandomAccessFile raf) throws IOException {
        raf.seek(0);
        headerItem.write(raf);

        for (MapItem item : mapList.list) {
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
    }

    private void updateOffsets() {
        if (headerItem.stringIdsOff != 0x70) {
            throw new RuntimeException("baa");
        }
        headerItem.updateOffsets();
        mapList.updateOffsets();

        updateOffsets(stringIdItems);
        updateOffsets(classDefItems);
        updateOffsets(typeIdItems);
        updateOffsets(fieldIdItems);
        updateOffsets(methodIdItems);
        updateOffsets(protoIdItems);
        updateOffsets(debugInfoItems);
        updateOffsets(stringDataItems);
        updateOffsets(classDataItems);
        updateOffsets(codeItems);

        if (headerItem.stringIdsOff != 0x70) {
            throw new RuntimeException("boo");
        }
    }

    private <T extends Streamable> void updateOffsets(T[] array) {
        if (array == null) {
            return;
        }
        for (T s : array) {
            s.updateOffsets();
        }
    }

    private void updateMessageDigest(RandomAccessFile raf) throws IOException {
        raf.seek(8 + 4 + 20);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            while (true) {
                try {
                    md.update(raf.readByte());
                } catch (EOFException e) {
                    break;
                }
            }
            headerItem.signature = md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateChecksum(RandomAccessFile raf) throws IOException {
        raf.seek(8 + 4);
        Adler32 checksum = new Adler32();
        while (true) {
            try {
                checksum.update(raf.readByte());
            } catch (EOFException e) {
                break;
            }
        }
        headerItem.checksum = (int) checksum.getValue();
    }

    String getString(long idx)  {
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
        @F(idx=8) @Offset int linkOff;
        @F(idx=9) @Offset int mapOff;
        @F(idx=10) int stringIdsSize;
        @F(idx=11) @Offset int stringIdsOff;
        @F(idx=12) int typeIdsSize;
        @F(idx=13) @Offset int typeIdsOff;
        @F(idx=14) int protoIdsSize;
        @F(idx=15) @Offset int protoIdsOff;
        @F(idx=16) int fieldIdsSize;
        @F(idx=17) @Offset int fieldIdsOff;
        @F(idx=18) int methodIdsSize;
        @F(idx=19) @Offset int methodIdsOff;
        @F(idx=20) int classDefsSize;
        @F(idx=21) @Offset int classDefsOff;
        @F(idx=22) int dataSize;
        @F(idx=23) @Offset int dataOff;

        public HeaderItem(DexFileReader dexFileReader) {
            super(dexFileReader);
        }

        @Override
        public void readImpl(RandomAccessFile raf) throws IOException {
            super.readImpl(raf);
            Log.i("DexFileReader", "checksum is: " + checksum + " " + String.format("%8x", checksum));
        }
    }

    MapItem getMapItem(ItemType itemType) {
        for (int i = 0; i < mapList.size; i++) {
            if (mapList.list[i].type == itemType.getMapValue()) {
                return mapList.list[i];
            }
        }
        return null;
    }

    static class MapList extends Streamable {
        @F(idx=1) int size;
        @F(idx=2, sizeIdx=1) MapItem[] list;

        public MapList(DexFileReader dexFileReader) {
            super(dexFileReader);
        }
    }

    static class MapItem extends Streamable {
        @F(idx=1) short type;
        @F(idx=2) short unused;
        @F(idx=3) int size;
        @F(idx=4) @Offset int offset;

        public MapItem(DexFileReader dexFileReader) {
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

    static class TypeIdItem extends Streamable {
        @F(idx=1) int descriptorIdx; // a  pointer to string_ids

        public TypeIdItem(DexFileReader dexFileReader) {
            super(dexFileReader);
        }

        public String getString() throws IOException{
            return dexFileReader.getString(descriptorIdx);
        }
    }

    static class EncodedField extends Streamable {
        @F(idx=1, uleb=true) int fieldIdxDiff;
        @F(idx=2, uleb=true) int accessFlags;

        @Override
        public boolean isAligned() {
            return false;
        }

        public EncodedField(DexFileReader dexFileReader) {
            super(dexFileReader);
        }
    }

    static class EncodedMethod extends Streamable {
        @F(idx=1, uleb=true) int methodIdxDiff;
        @F(idx=2, uleb=true) int accessFlags;
        @F(idx=3, uleb=true) @Offset int codeOff;

        @Override
        public boolean isAligned() {
            return false;
        }

        public EncodedMethod(DexFileReader dexFileReader) {
            super(dexFileReader);
        }

        @Override
        public void readImpl(RandomAccessFile raf) throws IOException {
            super.readImpl(raf);
            Log.i("DexFileReader", "Read encoded method as: " + methodIdxDiff + " " + accessFlags + " " + codeOff);
        }
    }

    public CodeItem getCodeItem(EncodedMethod method) {
        for (CodeItem codeItem : codeItems) {
            if (codeItem.getOrigOffset() == method.codeOff) {
                return codeItem;
            }
        }

        throw new RuntimeException("could not find codeItem");
    }

    static class TryItem extends Streamable {
        @F(idx=1) int startAddr;
        @F(idx=2) short insnCount;
        @F(idx=3) @Offset short handlerOff;
        public TryItem(DexFileReader dexFileReader) {
            super(dexFileReader);
        }
    }

    static class EncodedCatchHandlerList extends Streamable {
        @F(idx=1, uleb=true) int size;
        @F(idx=2, sizeIdx=1) EncodedCatchHandler[] list;

        public EncodedCatchHandlerList(DexFileReader dexFileReader) {
            super(dexFileReader);
        }
    }

    static class EncodedCatchHandler extends Streamable {
        long size;
        EncodedTypeAddrPair[] handlers;
        long catchAllAddr;

        public EncodedCatchHandler(DexFileReader dexFileReader) {
            super(dexFileReader);
        }

        @Override
        public void readImpl(RandomAccessFile raf) throws IOException {
            size = RafUtil.readSLeb128(raf);

            // handle negative numbers here.
            if (size != 0) {

                throw new RuntimeException("unexpected: " + size);
            }

            handlers = readArray(Math.abs((int) size), EncodedTypeAddrPair.class, this, raf);
            catchAllAddr = RafUtil.readULeb128(raf);
        }

    }

    static class EncodedTypeAddrPair extends Streamable {
        @F(idx=1, uleb=true) long typeIdx;
        @F(idx=1, uleb=true) long addr;

        public EncodedTypeAddrPair(DexFileReader dexFileReader) {
            super(dexFileReader);
        }
    }

    static class ClassDataItem extends Streamable {
        @F(idx=1, uleb=true) int staticFieldsSize;
        @F(idx=2, uleb=true) int instanceFieldsSize;
        @F(idx=3, uleb=true) int directMethodsSize;
        @F(idx=4, uleb=true) int virtualMethodsSize;

        @F(idx=5, sizeIdx=1) EncodedField[] staticFields;
        @F(idx=6, sizeIdx=2) EncodedField[] instanceFields;
        @F(idx=7, sizeIdx=3) EncodedMethod[] directMethods;
        @F(idx=8, sizeIdx=4) EncodedMethod[] virtualMethods;

        @Override
        public boolean isAligned() {
            return false;
        }

        public ClassDataItem(DexFileReader dexFileReader) {
            super(dexFileReader);
        }
    }

    public ClassDataItem getClassDataItem(ClassDefItem classDefItem) {
        for (ClassDataItem dataItem : classDataItems) {
            if (dataItem.getOrigOffset() == classDefItem.classDataOff) {
                return dataItem;
            }
        }

        throw new RuntimeException("could not find data item");
    }

    static class ClassDefItem extends Streamable {
        @F(idx=1) int classIdx;
        @F(idx=2) int accessFlags;
        @F(idx=3) int superclassIdx;
        @F(idx=4) @Offset int interfacesOff;
        @F(idx=5) int sourceFileIdx;
        @F(idx=6) @Offset int annotationsOff;
        @F(idx=7) @Offset int classDataOff;
        @F(idx=8) @Offset int staticValuesOff;

        public ClassDefItem(DexFileReader dexFileReader) {
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
        @F(idx=1) @Offset int stringDataOff;
        int originalIndex = -1;

        public StringIdItem(DexFileReader reader) {
            super(reader);
        }
    }

    static class FieldIdItem extends Streamable {
        @F(idx=1) short classIdx;
        @F(idx=2) short typeIdx;
        @F(idx=3) int nameIdx;

        public FieldIdItem(DexFileReader dexFileReader) {
            super(dexFileReader);
        }
    }

    static class MethodIdItem extends Streamable {
        @F(idx=1) short classIdx;
        @F(idx=2) short protoIdx;
        @F(idx=3) int nameIdx;

        public MethodIdItem(DexFileReader dexFileReader) {
            super(dexFileReader);
        }
    }

    static class ProtoIdItem extends Streamable {
        @F(idx=1) int shortyIdx;
        @F(idx=2) int returnTypeIdx;
        @F(idx=3) @Offset int parametersOff;

        public ProtoIdItem(DexFileReader dexFileReader) {
            super(dexFileReader);
        }
    }

    static class DebugInfoItem extends Streamable {
        int lineStart;
        int parametersSize;
        int[] parameterNames;

        public DebugInfoItem(DexFileReader dexFileReader) {
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
