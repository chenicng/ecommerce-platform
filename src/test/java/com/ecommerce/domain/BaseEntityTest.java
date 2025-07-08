package com.ecommerce.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class BaseEntityTest {

    // Concrete implementation for testing
    private static class TestEntity extends BaseEntity {
        public TestEntity() {
            super();
        }
    }

    private TestEntity entity;

    @BeforeEach
    void setUp() {
        entity = new TestEntity();
    }

    @Test
    void testEntityCreation() {
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
        assertEquals(0L, entity.getVersion());
        assertNull(entity.getId());
    }

    @Test
    void testInitialTimestamps() {
        LocalDateTime now = LocalDateTime.now();
        
        // CreatedAt should be close to now (within 1 second)
        assertTrue(entity.getCreatedAt().isBefore(now.plusSeconds(1)));
        assertTrue(entity.getCreatedAt().isAfter(now.minusSeconds(1)));
        
        // UpdatedAt should be close to now (within 1 second)
        assertTrue(entity.getUpdatedAt().isBefore(now.plusSeconds(1)));
        assertTrue(entity.getUpdatedAt().isAfter(now.minusSeconds(1)));
    }

    @Test
    void testInitialTimestampsAreEqual() {
        // Initially, createdAt and updatedAt should be very close
        // (created in the same constructor call)
        assertTrue(Math.abs(entity.getCreatedAt().getNano() - entity.getUpdatedAt().getNano()) < 1000000);
    }

    @Test
    void testInitialVersion() {
        assertEquals(0L, entity.getVersion());
    }

    @Test
    void testMarkAsUpdated() throws InterruptedException {
        LocalDateTime originalUpdatedAt = entity.getUpdatedAt();
        Long originalVersion = entity.getVersion();
        
        // Small delay to ensure timestamp difference
        Thread.sleep(1);
        
        entity.markAsUpdated();
        
        assertTrue(entity.getUpdatedAt().isAfter(originalUpdatedAt));
        assertEquals(originalVersion + 1, entity.getVersion());
    }

    @Test
    void testMarkAsUpdatedMultipleTimes() throws InterruptedException {
        assertEquals(0L, entity.getVersion());
        
        entity.markAsUpdated();
        assertEquals(1L, entity.getVersion());
        
        Thread.sleep(1);
        entity.markAsUpdated();
        assertEquals(2L, entity.getVersion());
        
        Thread.sleep(1);
        entity.markAsUpdated();
        assertEquals(3L, entity.getVersion());
    }

    @Test
    void testCreatedAtRemainsUnchanged() throws InterruptedException {
        LocalDateTime originalCreatedAt = entity.getCreatedAt();
        
        Thread.sleep(1);
        entity.markAsUpdated();
        
        assertEquals(originalCreatedAt, entity.getCreatedAt());
    }

    @Test
    void testIdGetterAndSetter() {
        assertNull(entity.getId());
        
        Long testId = 123L;
        entity.setId(testId);
        
        assertEquals(testId, entity.getId());
    }

    @Test
    void testIdSetterWithNull() {
        entity.setId(456L);
        assertEquals(456L, entity.getId());
        
        entity.setId(null);
        assertNull(entity.getId());
    }

    @Test
    void testEqualsWithSameId() {
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();
        
        entity1.setId(100L);
        entity2.setId(100L);
        
        assertEquals(entity1, entity2);
        assertTrue(entity1.equals(entity2));
        assertTrue(entity2.equals(entity1));
    }

    @Test
    void testEqualsWithDifferentIds() {
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();
        
        entity1.setId(100L);
        entity2.setId(200L);
        
        assertNotEquals(entity1, entity2);
        assertFalse(entity1.equals(entity2));
        assertFalse(entity2.equals(entity1));
    }

    @Test
    void testEqualsWithNullIds() {
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();
        
        // Both have null IDs
        assertEquals(entity1, entity2);
    }

    @Test
    void testEqualsWithOneNullId() {
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();
        
        entity1.setId(100L);
        // entity2 has null ID
        
        assertNotEquals(entity1, entity2);
        assertNotEquals(entity2, entity1);
    }

    @Test
    void testEqualsSameInstance() {
        assertTrue(entity.equals(entity));
    }

    @Test
    void testEqualsWithNull() {
        assertFalse(entity.equals(null));
    }

    @Test
    void testEqualsWithDifferentType() {
        String notAnEntity = "test";
        assertFalse(entity.equals(notAnEntity));
    }

    @Test
    void testHashCodeConsistency() {
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();
        
        entity1.setId(100L);
        entity2.setId(100L);
        
        assertEquals(entity1.hashCode(), entity2.hashCode());
    }

    @Test
    void testHashCodeWithNullId() {
        TestEntity entityWithNullId = new TestEntity();
        
        // Should not throw exception
        int hashCode = entityWithNullId.hashCode();
        assertNotNull(hashCode);
    }

    @Test
    void testHashCodeDifferentForDifferentIds() {
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();
        
        entity1.setId(100L);
        entity2.setId(200L);
        
        // Hash codes should be different (though not guaranteed)
        assertNotEquals(entity1.hashCode(), entity2.hashCode());
    }

    @Test
    void testVersionIncrementBehavior() {
        assertEquals(0L, entity.getVersion());
        
        // Version should increment with each update
        for (int i = 1; i <= 10; i++) {
            entity.markAsUpdated();
            assertEquals((long) i, entity.getVersion());
        }
    }

    @Test
    void testTimestampProgression() throws InterruptedException {
        LocalDateTime time1 = entity.getUpdatedAt();
        
        Thread.sleep(1);
        entity.markAsUpdated();
        LocalDateTime time2 = entity.getUpdatedAt();
        
        Thread.sleep(1);
        entity.markAsUpdated();
        LocalDateTime time3 = entity.getUpdatedAt();
        
        assertTrue(time1.isBefore(time2));
        assertTrue(time2.isBefore(time3));
    }
}