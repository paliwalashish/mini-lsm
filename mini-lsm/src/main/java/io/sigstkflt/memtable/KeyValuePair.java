package io.sigstkflt.memtable;

public record KeyValuePair(ByteArrayWrapper keyWrapper, byte[] data){}