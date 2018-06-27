package com.tdrhq.eyepatch.renamer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.util.List;

public abstract class Streamable {
    private long origOffset = -1;
    private long writeOffset = -1;
    protected DexFileReader dexFileReader;

    public Streamable(DexFileReader dexFileReader) {
        this.dexFileReader = dexFileReader;
    }

    public void read(RandomAccessFile raf) throws IOException {
        origOffset = raf.getFilePointer();
        readImpl(raf);
    }

    public long getOrigOffset() {
        return origOffset;
    }

    public void write(RandomAccessFile output) throws IOException {
        writeOffset = output.getFilePointer();
        writeImpl();
    }

    void readObject(RandomAccessFile raf) throws IOException {
        Class klass = this.getClass();
        if (klass == Streamable.class) {
            throw new RuntimeException("unexpected");
        }
        List<Field> fields = AnnotationUtil.getAnnotatedFields(klass);
        for (Field f : fields) {
            try {
                if (f.getType() == int.class) {
                    if (f.getAnnotation(F.class).uleb()) {
                        f.set(this, RafUtil.readULeb128(raf));
                    } else {
                        f.set(this, RafUtil.readUInt(raf));
                    }
                } else if (f.getType() == short.class) {
                    f.set(this, RafUtil.readUShort(raf));
                } else if (f.getType() == byte[].class) {
                    byte[] arr = (byte[]) f.get(this);
                    if (arr == null) {
                        throw new NullPointerException();
                    }
                    raf.read(arr);
                } else if (f.getType() == short[].class) {
                    int size = AnnotationUtil.getSizeFromSizeIdx(this, f);
                    f.set(this, RafUtil.readShortArray(size, raf));
                } else if (f.getType().isArray()) {
                    Class type = f.getType();
                    Class<? extends Streamable> componentType =
                            (Class<? extends Streamable>) type.getComponentType();
                    int size = AnnotationUtil.getSizeFromSizeIdx(this, f);
                    f.set(this, DexFileReader.readArray(size, componentType, dexFileReader, raf));
                }
                else {
                    throw new UnsupportedOperationException();
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void readImpl(RandomAccessFile raf) throws IOException {
        readObject(raf);
    }

    public void writeImpl() throws IOException {
        throw new UnsupportedOperationException();
    }
}
