package com.ecommerce.domain.settlement;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SettlementStatusTest {

    @Test
    void testAllEnumValues() {
        SettlementStatus[] statuses = SettlementStatus.values();
        
        assertEquals(3, statuses.length);
        assertEquals(SettlementStatus.MATCHED, statuses[0]);
        assertEquals(SettlementStatus.SURPLUS, statuses[1]);
        assertEquals(SettlementStatus.DEFICIT, statuses[2]);
    }

    @Test
    void testMatchedStatus() {
        SettlementStatus status = SettlementStatus.MATCHED;
        
        assertEquals("MATCHED", status.name());
        assertEquals(SettlementStatus.MATCHED, status);
    }

    @Test
    void testSurplusStatus() {
        SettlementStatus status = SettlementStatus.SURPLUS;
        
        assertEquals("SURPLUS", status.name());
        assertEquals(SettlementStatus.SURPLUS, status);
    }

    @Test
    void testDeficitStatus() {
        SettlementStatus status = SettlementStatus.DEFICIT;
        
        assertEquals("DEFICIT", status.name());
        assertEquals(SettlementStatus.DEFICIT, status);
    }

    @Test
    void testValueOfMethod() {
        assertEquals(SettlementStatus.MATCHED, SettlementStatus.valueOf("MATCHED"));
        assertEquals(SettlementStatus.SURPLUS, SettlementStatus.valueOf("SURPLUS"));
        assertEquals(SettlementStatus.DEFICIT, SettlementStatus.valueOf("DEFICIT"));
    }

    @Test
    void testValueOfWithInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> {
            SettlementStatus.valueOf("INVALID_STATUS");
        });
    }

    @Test
    void testValueOfWithNullValue() {
        assertThrows(NullPointerException.class, () -> {
            SettlementStatus.valueOf(null);
        });
    }

    @Test
    void testEnumEquality() {
        SettlementStatus status1 = SettlementStatus.MATCHED;
        SettlementStatus status2 = SettlementStatus.MATCHED;
        SettlementStatus status3 = SettlementStatus.SURPLUS;
        
        assertEquals(status1, status2);
        assertNotEquals(status1, status3);
        assertTrue(status1 == status2);
        assertFalse(status1 == status3);
    }

    @Test
    void testEnumOrdinal() {
        assertEquals(0, SettlementStatus.MATCHED.ordinal());
        assertEquals(1, SettlementStatus.SURPLUS.ordinal());
        assertEquals(2, SettlementStatus.DEFICIT.ordinal());
    }

    @Test
    void testToString() {
        assertEquals("MATCHED", SettlementStatus.MATCHED.toString());
        assertEquals("SURPLUS", SettlementStatus.SURPLUS.toString());
        assertEquals("DEFICIT", SettlementStatus.DEFICIT.toString());
    }
}