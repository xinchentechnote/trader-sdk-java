package com.xinchentechnote.exchange.common;

import org.junit.Test;

import static org.junit.Assert.*;

public class FrontInfoFieldTest {

    @Test
    public void testParseValidAddress() {
        FrontInfoField field = new FrontInfoField("tcp://127.0.0.1:9010");
        assertEquals("tcp", field.getProtocol());
        assertEquals("127.0.0.1", field.getIp());
        assertEquals(9010, field.getPort());
    }

    @Test
    public void testParseValidAddressWithOtherProtocol() {
        FrontInfoField field = new FrontInfoField("ssl://10.0.0.1:8080");
        assertEquals("ssl", field.getProtocol());
        assertEquals("10.0.0.1", field.getIp());
        assertEquals(8080, field.getPort());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNullThrows() {
        new FrontInfoField(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseEmptyThrows() {
        new FrontInfoField("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseBlankThrows() {
        new FrontInfoField("   ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseWithoutProtocolThrows() {
        new FrontInfoField("127.0.0.1:9010");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseWithoutPortThrows() {
        new FrontInfoField("tcp://127.0.0.1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNonNumericPortThrows() {
        new FrontInfoField("tcp://127.0.0.1:abc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParsePortOverflowThrows() {
        new FrontInfoField("tcp://127.0.0.1:99999");
    }
}
