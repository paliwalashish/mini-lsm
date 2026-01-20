package io.sigstkflt.memtable;

public interface MemTableIterator {
    boolean hasNext();

    KeyValuePair next();
}
