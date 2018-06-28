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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

    List<StringIdItem> stringIdItems = null;
    private RandomAccessFile raf;
    ClassDefItem[] classDefItems = null;
    TypeIdItem[] typeIdItems = null;
    FieldIdItem[] fieldIdItems = null;
    MethodIdItem[] methodIdItems = null;
    ProtoIdItem[] protoIdItems = null;
    DebugInfoItem[] debugInfoItems;
    ArrayList<_StringDataItem> stringDataItems = null;
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
                stringIdItems = newList(readArray(
                        (int) headerItem.stringIdsSize,
                        StringIdItem.class,
                        this,
                        raf));

                for (int i = 0; i < stringIdItems.size(); i++) {
                    stringIdItems.get(i).originalIndex = i;
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
                stringDataItems = newList(readArray(item.size, _StringDataItem.class, this, raf));
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

    private <T> ArrayList<T> newList(T[] ts) {
        ArrayList<T> ret = new ArrayList<T>();
        for (T t : ts) {
            ret.add(t);
        }
        return ret;
    }

    @Override
    public int getUpdatedStringIndex(int originalIndex) {
        for (int i = 0; i < stringIdItems.size(); i++) {
            if (originalIndex == stringIdItems.get(i).originalIndex) {
                return i;
            }
        }

        throw new RuntimeException("no such id");
    }

    public void addString(String val) {
        _StringDataItem newDataItem = new _StringDataItem(this);
        newDataItem.decoded = val;
        stringDataItems.add(newDataItem);

        int newOffset = (insertedOffsets --);
        newDataItem.setOrigOffset(newOffset);

        // now to figure out where to insert the idItem.
        int insertPos = 0;
        for (; insertPos < stringIdItems.size(); insertPos++) {
            if (compareStrings(
                        val,
                        getString(insertPos))) {
                break;
            }

        }

        StringIdItem newItem =  new StringIdItem(this);
        newItem.stringDataOff = (int) newDataItem.getOrigOffset();
        newItem.originalIndex = -1;

        stringIdItems.add(insertPos, newItem);

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
                writeArray(stringIdItems.toArray(new StringIdItem[1]), raf);
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
                writeArray(stringDataItems.toArray(new _StringDataItem[1]), raf);
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

        updateOffsets(stringIdItems.toArray(new StringIdItem[0]));
        updateOffsets(classDefItems);
        updateOffsets(typeIdItems);
        updateOffsets(fieldIdItems);
        updateOffsets(methodIdItems);
        updateOffsets(protoIdItems);
        updateOffsets(debugInfoItems);
        updateOffsets(stringDataItems.toArray(new _StringDataItem[1]));
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
        long off = stringIdItems.get((int) idx).stringDataOff;
        for (_StringDataItem  dataItem : stringDataItems) {
            if (dataItem.getOrigOffset() == off) {
                return dataItem.decoded;
            }
        }

        throw new RuntimeException("could not find data for this string");
    }

    MapItem getMapItem(ItemType itemType) {
        for (int i = 0; i < mapList.size; i++) {
            if (mapList.list[i].type == itemType.getMapValue()) {
                return mapList.list[i];
            }
        }
        return null;
    }

    public CodeItem getCodeItem(EncodedMethod method) {
        for (CodeItem codeItem : codeItems) {
            if (codeItem.getOrigOffset() == method.codeOff) {
                return codeItem;
            }
        }

        throw new RuntimeException("could not find codeItem");
    }

    public ClassDataItem getClassDataItem(ClassDefItem classDefItem) {
        for (ClassDataItem dataItem : classDataItems) {
            if (dataItem.getOrigOffset() == classDefItem.classDataOff) {
                return dataItem;
            }
        }

        throw new RuntimeException("could not find data item");
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

    interface NameProvider {
        public String rename(String input);
    }
}
