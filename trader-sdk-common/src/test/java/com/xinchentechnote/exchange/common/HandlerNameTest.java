package com.xinchentechnote.exchange.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HandlerNameTest {

    @Test
    public void testConstants() {
        assertEquals("idleHandler", HandlerName.IDLE);
        assertEquals("frameHandler", HandlerName.FRAME);
        assertEquals("messageHandler", HandlerName.MESSAGE);
    }
}
