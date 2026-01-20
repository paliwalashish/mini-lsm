package io.sigstkflt.memtable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.junit.jupiter.api.Assertions.*;

class DefaultMemtableIteratorTest {

    private ConcurrentSkipListMap<ByteArrayWrapper, byte[]> map;

    @BeforeEach
    void setUp() {
        map = new ConcurrentSkipListMap<>();
    }

    // Helper methods
    private ByteArrayWrapper wrap(String s) {
        return new ByteArrayWrapper(s.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] toBytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    private void putEntry(String key, String value) {
        map.put(wrap(key), toBytes(value));
    }

    // ==================== Full Iteration Constructor Tests ====================

    @Nested
    @DisplayName("Full memtable iteration (no range)")
    class FullIterationTests {

        @Test
        @DisplayName("should return false for hasNext on empty memtable")
        void testEmptyMemtableHasNext() {
            DefaultMemTableIterator iterator = new DefaultMemTableIterator(map);

            assertFalse(iterator.hasNext());
        }

        @Test
        @DisplayName("should iterate over single element")
        void testSingleElementIteration() {
            putEntry("key1", "value1");

            DefaultMemTableIterator iterator = new DefaultMemTableIterator(map);

            assertTrue(iterator.hasNext());
            KeyValuePair pair = iterator.next();
            assertArrayEquals(toBytes("key1"), pair.keyWrapper().getValue());
            assertArrayEquals(toBytes("value1"), pair.data());
            assertFalse(iterator.hasNext());
        }

        @Test
        @DisplayName("should iterate over multiple elements in sorted order")
        void testMultipleElementsInSortedOrder() {
            putEntry("charlie", "3");
            putEntry("alpha", "1");
            putEntry("bravo", "2");

            DefaultMemTableIterator iterator = new DefaultMemTableIterator(map);

            List<String> keys = new ArrayList<>();
            while (iterator.hasNext()) {
                KeyValuePair pair = iterator.next();
                keys.add(new String(pair.keyWrapper().getValue(), StandardCharsets.UTF_8));
            }

            assertEquals(List.of("alpha", "bravo", "charlie"), keys);
        }

        @Test
        @DisplayName("should iterate over all entries")
        void testIteratesOverAllEntries() {
            for (int i = 0; i < 100; i++) {
                putEntry("key" + String.format("%03d", i), "value" + i);
            }

            DefaultMemTableIterator iterator = new DefaultMemTableIterator(map);

            int count = 0;
            while (iterator.hasNext()) {
                iterator.next();
                count++;
            }

            assertEquals(100, count);
        }

        @Test
        @DisplayName("should throw NoSuchElementException when exhausted")
        void testThrowsNoSuchElementExceptionWhenExhausted() {
            putEntry("key1", "value1");

            DefaultMemTableIterator iterator = new DefaultMemTableIterator(map);
            iterator.next();

            assertThrows(NoSuchElementException.class, iterator::next);
        }
    }

    // ==================== Range Iteration Constructor Tests ====================

    @Nested
    @DisplayName("Range iteration (with beginKey and endKey)")
    class RangeIterationTests {

        @BeforeEach
        void setUpRangeData() {
            putEntry("a", "1");
            putEntry("b", "2");
            putEntry("c", "3");
            putEntry("d", "4");
            putEntry("e", "5");
        }

        @Test
        @DisplayName("should iterate full map when both keys are null")
        void testBothKeysNull() {
            DefaultMemTableIterator iterator = new DefaultMemTableIterator(map, null, null);

            List<String> keys = collectKeys(iterator);

            assertEquals(List.of("a", "b", "c", "d", "e"), keys);
        }

        @Test
        @DisplayName("should iterate from beginKey to end when endKey is null")
        void testOnlyBeginKey() {
            DefaultMemTableIterator iterator = new DefaultMemTableIterator(map, wrap("c"), null);

            List<String> keys = collectKeys(iterator);

            assertEquals(List.of("c", "d", "e"), keys);
        }

        @Test
        @DisplayName("should iterate from start to endKey when beginKey is null")
        void testOnlyEndKey() {
            DefaultMemTableIterator iterator = new DefaultMemTableIterator(map, null, wrap("c"));

            List<String> keys = collectKeys(iterator);

            // headMap is exclusive of endKey
            assertEquals(List.of("a", "b"), keys);
        }

        @Test
        @DisplayName("should iterate within inclusive range when both keys specified")
        void testBothKeysSpecified() {
            DefaultMemTableIterator iterator = new DefaultMemTableIterator(map, wrap("b"), wrap("d"));

            List<String> keys = collectKeys(iterator);

            assertEquals(List.of("b", "c", "d"), keys);
        }

        @Test
        @DisplayName("should handle beginKey that doesn't exist in map")
        void testBeginKeyNotInMap() {
            // "bb" doesn't exist, should start from "c"
            DefaultMemTableIterator iterator = new DefaultMemTableIterator(map, wrap("bb"), null);

            List<String> keys = collectKeys(iterator);

            assertEquals(List.of("c", "d", "e"), keys);
        }

        @Test
        @DisplayName("should handle endKey that doesn't exist in map")
        void testEndKeyNotInMap() {
            // "cc" doesn't exist, should include up to but not including "cc"
            DefaultMemTableIterator iterator = new DefaultMemTableIterator(map, null, wrap("cc"));

            List<String> keys = collectKeys(iterator);

            assertEquals(List.of("a", "b", "c"), keys);
        }

        @Test
        @DisplayName("should return empty iterator when range has no elements")
        void testEmptyRange() {
            // Range between "aa" and "ab" has no elements
            DefaultMemTableIterator iterator = new DefaultMemTableIterator(map, wrap("aa"), wrap("ab"));

            assertFalse(iterator.hasNext());
        }

        @Test
        @DisplayName("should return single element when beginKey equals endKey and exists")
        void testSingleElementRange() {
            DefaultMemTableIterator iterator = new DefaultMemTableIterator(map, wrap("c"), wrap("c"));

            List<String> keys = collectKeys(iterator);

            assertEquals(List.of("c"), keys);
        }

        @Test
        @DisplayName("should handle beginKey after all elements")
        void testBeginKeyAfterAllElements() {
            DefaultMemTableIterator iterator = new DefaultMemTableIterator(map, wrap("z"), null);

            assertFalse(iterator.hasNext());
        }

        @Test
        @DisplayName("should handle endKey before all elements")
        void testEndKeyBeforeAllElements() {
            // headMap with key before "a" should return empty
            DefaultMemTableIterator iterator = new DefaultMemTableIterator(map, null, wrap("0"));

            assertFalse(iterator.hasNext());
        }
    }

    // ==================== KeyValuePair Tests ====================

    @Nested
    @DisplayName("KeyValuePair returned by next()")
    class KeyValuePairTests {

        @Test
        @DisplayName("should return correct key wrapper")
        void testKeyWrapper() {
            putEntry("testKey", "testValue");

            DefaultMemTableIterator iterator = new DefaultMemTableIterator(map);
            KeyValuePair pair = iterator.next();

            assertArrayEquals(toBytes("testKey"), pair.keyWrapper().getValue());
        }

        @Test
        @DisplayName("should return correct data")
        void testData() {
            putEntry("testKey", "testValue");

            DefaultMemTableIterator iterator = new DefaultMemTableIterator(map);
            KeyValuePair pair = iterator.next();

            assertArrayEquals(toBytes("testValue"), pair.data());
        }

        @Test
        @DisplayName("should return different KeyValuePair instances for each next() call")
        void testDifferentInstances() {
            putEntry("key1", "value1");
            putEntry("key2", "value2");

            DefaultMemTableIterator iterator = new DefaultMemTableIterator(map);
            KeyValuePair pair1 = iterator.next();
            KeyValuePair pair2 = iterator.next();

            assertNotSame(pair1, pair2);
        }
    }

    // ==================== Edge Cases ====================

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle binary keys")
        void testBinaryKeys() {
            byte[] key1 = new byte[]{0, 1, 2};
            byte[] key2 = new byte[]{0, 1, 3};
            byte[] value1 = toBytes("value1");
            byte[] value2 = toBytes("value2");

            map.put(new ByteArrayWrapper(key2), value2);
            map.put(new ByteArrayWrapper(key1), value1);

            DefaultMemTableIterator iterator = new DefaultMemTableIterator(map);

            KeyValuePair pair1 = iterator.next();
            assertArrayEquals(key1, pair1.keyWrapper().getValue());

            KeyValuePair pair2 = iterator.next();
            assertArrayEquals(key2, pair2.keyWrapper().getValue());
        }

        @Test
        @DisplayName("should handle empty key")
        void testEmptyKey() {
            byte[] emptyKey = new byte[0];
            byte[] value = toBytes("value");
            map.put(new ByteArrayWrapper(emptyKey), value);

            DefaultMemTableIterator iterator = new DefaultMemTableIterator(map);

            assertTrue(iterator.hasNext());
            KeyValuePair pair = iterator.next();
            assertEquals(0, pair.keyWrapper().getValue().length);
        }

        @Test
        @DisplayName("should handle empty value")
        void testEmptyValue() {
            putEntry("key", "");

            DefaultMemTableIterator iterator = new DefaultMemTableIterator(map);
            KeyValuePair pair = iterator.next();

            assertEquals(0, pair.data().length);
        }

        @Test
        @DisplayName("should handle keys with special characters")
        void testKeysWithSpecialCharacters() {
            putEntry("key\twith\ttabs", "value1");
            putEntry("key\nwith\nnewlines", "value2");
            putEntry("key with spaces", "value3");

            DefaultMemTableIterator iterator = new DefaultMemTableIterator(map);

            int count = 0;
            while (iterator.hasNext()) {
                iterator.next();
                count++;
            }

            assertEquals(3, count);
        }

        @Test
        @DisplayName("should handle high byte values in keys")
        void testHighByteValues() {
            byte[] key = new byte[]{(byte) 255, (byte) 254, (byte) 253};
            byte[] value = toBytes("highValue");
            map.put(new ByteArrayWrapper(key), value);

            DefaultMemTableIterator iterator = new DefaultMemTableIterator(map);

            assertTrue(iterator.hasNext());
            KeyValuePair pair = iterator.next();
            assertArrayEquals(key, pair.keyWrapper().getValue());
        }
    }

    // ==================== hasNext() Idempotency Tests ====================

    @Nested
    @DisplayName("hasNext() idempotency")
    class HasNextIdempotencyTests {

        @Test
        @DisplayName("hasNext() should be idempotent")
        void testHasNextIdempotent() {
            putEntry("key1", "value1");

            DefaultMemTableIterator iterator = new DefaultMemTableIterator(map);

            assertTrue(iterator.hasNext());
            assertTrue(iterator.hasNext());
            assertTrue(iterator.hasNext());

            iterator.next();

            assertFalse(iterator.hasNext());
            assertFalse(iterator.hasNext());
        }

        @Test
        @DisplayName("multiple hasNext() calls should not advance iterator")
        void testHasNextDoesNotAdvance() {
            putEntry("key1", "value1");
            putEntry("key2", "value2");

            DefaultMemTableIterator iterator = new DefaultMemTableIterator(map);

            // Call hasNext multiple times
            for (int i = 0; i < 5; i++) {
                iterator.hasNext();
            }

            // First next() should still return first element
            KeyValuePair pair = iterator.next();
            assertArrayEquals(toBytes("key1"), pair.keyWrapper().getValue());
        }
    }

    // ==================== Interface Compliance Tests ====================

    @Nested
    @DisplayName("Interface compliance")
    class InterfaceComplianceTests {

        @Test
        @DisplayName("should implement MemTableIterator interface")
        void testImplementsMemTableIterator() {
            DefaultMemTableIterator iterator = new DefaultMemTableIterator(map);

            assertTrue(iterator instanceof MemTableIterator);
        }
    }

    // ==================== Integration with SkipListMemTable ====================

    @Nested
    @DisplayName("Integration with SkipListMemTable")
    class IntegrationTests {

        @Test
        @DisplayName("iterator from SkipListMemTable should work correctly")
        void testIteratorFromSkipListMemTable() {
            SkipListMemtable memTable = new SkipListMemtable(1);
            memTable.put(toBytes("key1"), toBytes("value1"));
            memTable.put(toBytes("key2"), toBytes("value2"));
            memTable.put(toBytes("key3"), toBytes("value3"));

            MemTableIterator iterator = memTable.iterator();

            List<String> keys = new ArrayList<>();
            while (iterator.hasNext()) {
                KeyValuePair pair = iterator.next();
                keys.add(new String(pair.keyWrapper().getValue(), StandardCharsets.UTF_8));
            }

            assertEquals(List.of("key1", "key2", "key3"), keys);
        }
    }

    // Helper method to collect keys from iterator
    private List<String> collectKeys(DefaultMemTableIterator iterator) {
        List<String> keys = new ArrayList<>();
        while (iterator.hasNext()) {
            KeyValuePair pair = iterator.next();
            keys.add(new String(pair.keyWrapper().getValue(), StandardCharsets.UTF_8));
        }
        return keys;
    }
}
