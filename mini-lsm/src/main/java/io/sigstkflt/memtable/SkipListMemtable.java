package io.sigstkflt.memtable;

import java.util.Arrays;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

public class SkipListMemtable implements Memtable {
    private ConcurrentSkipListMap<ByteArrayWrapper, byte[]> map = new ConcurrentSkipListMap<>();
    private final int id;
    private AtomicLong estimatedSize = new AtomicLong(0);

    public SkipListMemtable(int id) {
        this.id = id;
    }

    @Override
    public byte[] get(byte[] key) {
        byte[] value = map.get(new ByteArrayWrapper(key));
        return value == null ? null : Arrays.copyOf(value, value.length);
    }

    @Override
    public void put(byte[] key, byte[] value) {
        map.put(new ByteArrayWrapper(key), value);
        estimatedSize.addAndGet(key.length + value.length);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public long approximateSize() {
        return estimatedSize.get();
    }

    public MemTableIterator iterator() {
        return new DefaultMemTableIterator(map);
    }

}
