package com.xinchentechnote.exchange.sse.impl;

import org.junit.Test;

import static org.junit.Assert.*;

public class HeartBtIntUtilTest {

    @Test
    public void testIsMin() {
        assertTrue(HeartBtIntUtil.isMin(HeartBtIntUtil.MIN));
        assertFalse(HeartBtIntUtil.isMin(HeartBtIntUtil.MIN + 1));
        assertFalse(HeartBtIntUtil.isMin(HeartBtIntUtil.MIN - 1));
        assertFalse(HeartBtIntUtil.isMin(0));
    }

    @Test
    public void testCalculateWithinBounds() {
        assertEquals(30, HeartBtIntUtil.calculate(30));
        assertEquals(HeartBtIntUtil.MIN, HeartBtIntUtil.calculate(HeartBtIntUtil.MIN));
        assertEquals(HeartBtIntUtil.MAX, HeartBtIntUtil.calculate(HeartBtIntUtil.MAX));
    }

    @Test
    public void testCalculateBelowMinClampsToMin() {
        assertEquals(HeartBtIntUtil.MIN, HeartBtIntUtil.calculate(0));
        assertEquals(HeartBtIntUtil.MIN, HeartBtIntUtil.calculate(1));
        assertEquals(HeartBtIntUtil.MIN, HeartBtIntUtil.calculate(HeartBtIntUtil.MIN - 1));
        assertEquals(HeartBtIntUtil.MIN, HeartBtIntUtil.calculate(-10));
    }

    @Test
    public void testCalculateAboveMaxClampsToMax() {
        assertEquals(HeartBtIntUtil.MAX, HeartBtIntUtil.calculate(HeartBtIntUtil.MAX + 1));
        assertEquals(HeartBtIntUtil.MAX, HeartBtIntUtil.calculate(1000));
    }
}
