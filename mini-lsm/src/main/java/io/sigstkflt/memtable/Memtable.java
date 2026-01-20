package io.sigstkflt.memtable;

public interface Memtable {
    byte[] get(byte[] key);
    void put(byte[] key, byte[] value);
    int getId();
    long approximateSize();
}
