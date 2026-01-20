package io.sigstkflt.memtable;

import java.util.Arrays;
import java.util.Objects;

/**
 * This class is created to avoid passing custom comparator
 * If using byte[], we need to pass custom Comparator
 */
public class ByteArrayWrapper implements Comparable<ByteArrayWrapper> {
    private byte[] value;
    public ByteArrayWrapper(byte[] value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ByteArrayWrapper that = (ByteArrayWrapper) o;
        return Objects.deepEquals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }

    @Override
    public int compareTo(ByteArrayWrapper o) {
        return Arrays.compareUnsigned(this.value, o.value);
    }

    public byte[] getValue() {
        return value;
    }
}
