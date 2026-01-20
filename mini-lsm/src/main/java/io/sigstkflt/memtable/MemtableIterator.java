package io.sigstkflt.memtable;

public interface MemtableIterator {
    boolean hasNext();

    KeyValuePair next();
}
