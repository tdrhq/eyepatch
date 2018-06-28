package com.tdrhq.eyepatch.renamer;

import android.util.Log;
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

    public final void read(RandomAccessFile raf) throws IOException {
        origOffset = raf.getFilePointer();
        readImpl(raf);
    }

    public long getOrigOffset() {
        return origOffset;
    }

    public void setOrigOffset(long _origOffset) {
        this.origOffset = _origOffset;
    }

    public boolean isAligned() {
        return true;
    }

    private void writeAlignment(RandomAccessFile output) throws IOException  {
        long len = 4L - output.getFilePointer() % 4;
        if (len == 4) {
            return;
        }
        byte[] out = new byte[(int) len];
        output.write(out);
    }

    public void write(RandomAccessFile output) throws IOException {
        if (isAligned()) {
            writeAlignment(output);
        }

        writeOffset = output.getFilePointer();
        dexFileReader.offsetMap.put((int) origOffset, (int) writeOffset);
        writeImpl(output);
    }

    final protected void readObject(RandomAccessFile raf) throws IOException {
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
                    raf.readFully(arr);
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

    final protected void writeObject(RandomAccessFile raf) throws IOException {
        Class klass = this.getClass();
        List<Field> fields = AnnotationUtil.getAnnotatedFields(klass);

        for (Field f : fields) {
            try {
                if (f.getType() == int.class) {
                    if (f.getAnnotation(F.class).uleb()) {
                        RafUtil.writeULeb128(raf, (int) f.get(this));
                    } else {
                        RafUtil.writeUInt(raf, (int) f.get(this));
                    }
                } else if (f.getType() == short.class) {
                    RafUtil.writeUShort(raf, (short) f.get(this));
                } else if (f.getType() == byte[].class) {
                    byte[] arr = (byte[]) f.get(this);
                    raf.write(arr);
                } else if (f.getType() == short[].class) {
                    RafUtil.writeShortArray(raf, (short[]) f.get(this));
                } else if (f.getType().isArray()) {
                    Class type = f.getType();
                    Class<? extends Streamable> componentType =
                            (Class<? extends Streamable>) type.getComponentType();
                    int size = AnnotationUtil.getSizeFromSizeIdx(this, f);

                    Object[] values = (Object[]) f.get(this);

                    if (size != values.length) {
                        throw new RuntimeException("array size doesn't match");
                    }

                    for (int i = 0; i < size; i++) {
                        ((Streamable) values[i]).write(raf);
                    }
                } else {
                    throw new UnsupportedOperationException("Type is: " + f.getType().toString());
                }

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public final void updateOffsets() {
        try {
            updateOffsetsImpl();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateOffsetsImpl() throws IllegalAccessException {
        for (Field f : AnnotationUtil.getAnnotatedFields(this.getClass())) {
            if (f.getAnnotation(Offset.class) != null) {
                try {
                    f.set(this,
                          dexFileReader.offsetMap.get(f.get(this)));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            if (f.getType().isArray()) {
                Class type = f.getType();
                Class componentType = type.getComponentType();

                if (Streamable.class.isAssignableFrom(componentType)) {
                    Streamable[] streamables = (Streamable[]) f.get(this);
                    for (Streamable streamable : streamables) {
                        streamable.updateOffsets();
                    }
                }
            }

            if (Streamable.class.isAssignableFrom(f.getType())) {
                ((Streamable) f.get(this)).updateOffsets();
            }
        }
    }

    protected void readImpl(RandomAccessFile raf) throws IOException {
        readObject(raf);
    }

    public void writeImpl(RandomAccessFile raf) throws IOException {
        writeObject(raf);
    }
}
