package com.xinchentechnote.exchange.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ApiLogLevelTest {

    @Test
    public void testEnumValues() {
        assertEquals(4, ApiLogLevel.values().length);
        assertEquals(ApiLogLevel.DEBUG, ApiLogLevel.values()[0]);
        assertEquals(ApiLogLevel.INFO, ApiLogLevel.values()[1]);
        assertEquals(ApiLogLevel.WARN, ApiLogLevel.values()[2]);
        assertEquals(ApiLogLevel.ERROR, ApiLogLevel.values()[3]);
    }
}
