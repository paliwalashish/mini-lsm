package io.sigstkflt.memtable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class SkipListMemtableTest {

    private SkipListMemtable memTable;

    @BeforeEach
    void setUp() {
        memTable = new SkipListMemtable(1);
    }

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("constructor should create memtable with given id")
    void testConstructor() {
        SkipListMemtable table = new SkipListMemtable(42);
        assertEquals(42, table.getId());
    }

    @Test
    @DisplayName("constructor with id 0 should work")
    void testConstructorWithZeroId() {
        SkipListMemtable table = new SkipListMemtable(0);
        assertEquals(0, table.getId());
    }

    @Test
    @DisplayName("constructor with negative id should work")
    void testConstructorWithNegativeId() {
        SkipListMemtable table = new SkipListMemtable(-1);
        assertEquals(-1, table.getId());
    }

    // ==================== Basic Operations Tests ====================

    @Test
    @DisplayName("put and get should store and retrieve a value")
    void testPutAndGet() {
        byte[] key = "key1".getBytes(StandardCharsets.UTF_8);
        byte[] value = "value1".getBytes(StandardCharsets.UTF_8);

        memTable.put(key, value);
        byte[] result = memTable.get(key);

        assertArrayEquals(value, result);
    }

    @Test
    @DisplayName("get should return null for non-existent key")
    void testGetNonExistentKey() {
        byte[] key = "nonexistent".getBytes(StandardCharsets.UTF_8);

        byte[] result = memTable.get(key);

        assertNull(result);
    }

    @Test
    @DisplayName("put should overwrite existing value")
    void testPutOverwrite() {
        byte[] key = "key1".getBytes(StandardCharsets.UTF_8);
        byte[] value1 = "value1".getBytes(StandardCharsets.UTF_8);
        byte[] value2 = "value2".getBytes(StandardCharsets.UTF_8);

        memTable.put(key, value1);
        memTable.put(key, value2);
        byte[] result = memTable.get(key);

        assertArrayEquals(value2, result);
    }

    // ==================== Delete Operation Tests ====================

    // ==================== ID Tests ====================

    @Test
    @DisplayName("getId should return the assigned id")
    void testGetId() {
        assertEquals(1, memTable.getId());
    }

    // ==================== Approximate Size Tests ====================

    @Test
    @DisplayName("approximateSize should return 0 for empty memtable")
    void testApproximateSizeEmpty() {
        long size = memTable.approximateSize();

        assertEquals(0, size);
    }

    // ==================== Multiple Operations Tests ====================

    @Test
    @DisplayName("should handle multiple put operations")
    void testMultiplePuts() {
        int index = 100;
        for (int i = 0; i < index; i++) {
            byte[] key = ("key" + i).getBytes(StandardCharsets.UTF_8);
            byte[] value = ("value" + i).getBytes(StandardCharsets.UTF_8);
            memTable.put(key, value);
        }

        for (int i = 0; i < index; i++) {
            byte[] key = ("key" + i).getBytes(StandardCharsets.UTF_8);
            byte[] expectedValue = ("value" + i).getBytes(StandardCharsets.UTF_8);
            assertArrayEquals(expectedValue, memTable.get(key));
        }
    }

    @Test
    @DisplayName("should handle put, delete, get sequence correctly")
    void testPutDeleteGetSequence() {
        byte[] key = "key".getBytes(StandardCharsets.UTF_8);
        byte[] value1 = "value1".getBytes(StandardCharsets.UTF_8);
        byte[] value2 = "value2".getBytes(StandardCharsets.UTF_8);

        memTable.put(key, value1);
        assertArrayEquals(value1, memTable.get(key));

        memTable.put(key, value2);
        assertArrayEquals(value2, memTable.get(key));
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("should handle empty key")
    void testEmptyKey() {
        byte[] key = new byte[0];
        byte[] value = "value".getBytes(StandardCharsets.UTF_8);

        memTable.put(key, value);
        byte[] result = memTable.get(key);

        assertArrayEquals(value, result);
    }

    @Test
    @DisplayName("should handle empty value")
    void testEmptyValue() {
        byte[] key = "key".getBytes(StandardCharsets.UTF_8);
        byte[] value = new byte[0];

        memTable.put(key, value);
        byte[] result = memTable.get(key);

        assertArrayEquals(value, result);
    }

    @Test
    @DisplayName("should handle binary data in key and value")
    void testBinaryData() {
        byte[] key = new byte[]{0, 1, 2, (byte) 255, (byte) 128};
        byte[] value = new byte[]{(byte) 200, (byte) 150, 0, 1};

        memTable.put(key, value);
        byte[] result = memTable.get(key);

        assertArrayEquals(value, result);
    }

    @Test
    @DisplayName("should handle large key and value")
    void testLargeKeyAndValue() {
        byte[] key = new byte[1024];
        byte[] value = new byte[10240];
        for (int i = 0; i < key.length; i++) {
            key[i] = (byte) (i % 256);
        }
        for (int i = 0; i < value.length; i++) {
            value[i] = (byte) (i % 256);
        }

        memTable.put(key, value);
        byte[] result = memTable.get(key);

        assertArrayEquals(value, result);
    }

    // ==================== Instance Independence Tests ====================

    @Test
    @DisplayName("different instances should be independent")
    void testInstanceIndependence() {
        SkipListMemtable memTable1 = new SkipListMemtable(1);
        SkipListMemtable memTable2 = new SkipListMemtable(2);

        byte[] key = "key".getBytes(StandardCharsets.UTF_8);
        byte[] value = "value".getBytes(StandardCharsets.UTF_8);

        memTable1.put(key, value);

        assertArrayEquals(value, memTable1.get(key));
        assertNull(memTable2.get(key));
    }

    @Test
    @DisplayName("different instances should have different ids")
    void testDifferentInstanceIds() {
        SkipListMemtable memTable1 = new SkipListMemtable(1);
        SkipListMemtable memTable2 = new SkipListMemtable(2);

        assertNotEquals(memTable1.getId(), memTable2.getId());
    }

    // ==================== Interface Compliance Tests ====================

    @Test
    @DisplayName("SkipListMemTable should implement MemTable interface")
    void testImplementsMemTable() {
        assertTrue(memTable instanceof Memtable);
    }
}
