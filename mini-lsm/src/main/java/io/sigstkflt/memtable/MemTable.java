package io.sigstkflt.memtable;

public interface MemTable {
    MemTable create(int id);
    byte[] get(byte[] key);
    void put(byte[] key, byte[] value);
    void delete(byte[] key);
    int getId();
    long approximateSize();
}
