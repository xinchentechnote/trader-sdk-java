package com.xinchentechnote.exchange.common;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ApiStatusTest {

    @Test
    public void testEnumValues() {
        List<ApiStatus> expected = Arrays.asList(
                ApiStatus.NEW,
                ApiStatus.CONNECTING,
                ApiStatus.CONNECTED,
                ApiStatus.LOGGED_IN,
                ApiStatus.LOGGING_OUT,
                ApiStatus.DISCONNECTED,
                ApiStatus.ERROR);
        assertEquals(expected, Arrays.asList(ApiStatus.values()));
        assertEquals(7, ApiStatus.values().length);
    }

    @Test
    public void testValueOf() {
        assertEquals(ApiStatus.CONNECTED, ApiStatus.valueOf("CONNECTED"));
        assertEquals(ApiStatus.LOGGED_IN, ApiStatus.valueOf("LOGGED_IN"));
    }
}
