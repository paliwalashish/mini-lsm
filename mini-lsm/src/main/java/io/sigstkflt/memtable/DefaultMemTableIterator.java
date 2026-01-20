package io.sigstkflt.memtable;

import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class DefaultMemTableIterator implements MemTableIterator {

    private ConcurrentSkipListMap<ByteArrayWrapper, byte[]> map;
    Iterator<Map.Entry<ByteArrayWrapper, byte[]>> iterator;

    /**
     * Defaulkt constructor to iterator complete Memtable like flushing to disk
     * @param memtable
     */
    public DefaultMemTableIterator(ConcurrentSkipListMap<ByteArrayWrapper, byte[]> memtable) {
        this.map = memtable;
        this.iterator = memtable.entrySet().iterator();
    }

    /**
     * Memtable to be used when querying a range of data from within the Memtable
     * To start we implement key's being inclusive, we can add the Bounds later
     *
     * @param memtable
     * @param beginKey
     * @param endKey
     */
    public DefaultMemTableIterator(ConcurrentSkipListMap<ByteArrayWrapper, byte[]> memtable, ByteArrayWrapper beginKey, ByteArrayWrapper endKey) {
        this.map = memtable;
        NavigableMap<ByteArrayWrapper, byte[]> iterableMap;

        if(beginKey == null && endKey == null) {
            iterableMap = memtable;
        } else if(beginKey == null ) {
            iterableMap = map.headMap(endKey);
        } else if(endKey == null ) {
            iterableMap = map.tailMap(beginKey);
        } else {
            iterableMap = map.subMap(beginKey, true, endKey, true);
        }
        iterator = iterableMap.entrySet().iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public KeyValuePair next() {
        Map.Entry<ByteArrayWrapper, byte[]> entry = iterator.next();
        return new KeyValuePair(entry.getKey(), entry.getValue());
    }
}
