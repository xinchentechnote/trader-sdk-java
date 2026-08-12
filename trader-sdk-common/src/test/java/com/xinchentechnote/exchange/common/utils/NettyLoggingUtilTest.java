package com.xinchentechnote.exchange.common.utils;

import io.netty.handler.logging.LogLevel;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class NettyLoggingUtilTest {

    private static final String ENABLED_ENV = "NETTY_LOGGING_ENABLED";
    private static final String LEVEL_ENV = "NETTY_LOGGING_LEVEL";
    private static final String HANDLER_NAME_ENV = "NETTY_LOGGING_HANDLER_NAME";

    @After
    public void tearDown() {
        System.clearProperty("netty.logging.enabled");
        System.clearProperty("netty.logging.level");
        System.clearProperty("netty.logging.handler.name");
    }

    @Test
    public void testLoggingDisabledByDefault() {
        assumeTrue("Env var overrides test", System.getenv(ENABLED_ENV) == null);
        assertFalse(NettyLoggingUtil.isLoggingEnabled());
    }

    @Test
    public void testLoggingEnabledViaSystemProperty() {
        assumeTrue("Env var overrides test", System.getenv(ENABLED_ENV) == null);
        System.setProperty("netty.logging.enabled", "true");
        assertTrue(NettyLoggingUtil.isLoggingEnabled());
    }

    @Test
    public void testLoggingEnabledViaSystemPropertyFalse() {
        assumeTrue("Env var overrides test", System.getenv(ENABLED_ENV) == null);
        System.setProperty("netty.logging.enabled", "false");
        assertFalse(NettyLoggingUtil.isLoggingEnabled());
    }

    @Test
    public void testDefaultLoggingLevel() {
        assumeTrue("Env var overrides test", System.getenv(LEVEL_ENV) == null);
        assertEquals(LogLevel.INFO, NettyLoggingUtil.getLoggingLevel());
    }

    @Test
    public void testLoggingLevelViaSystemProperty() {
        assumeTrue("Env var overrides test", System.getenv(LEVEL_ENV) == null);
        System.setProperty("netty.logging.level", "debug");
        assertEquals(LogLevel.DEBUG, NettyLoggingUtil.getLoggingLevel());
    }

    @Test
    public void testLoggingLevelViaSystemPropertyUppercase() {
        assumeTrue("Env var overrides test", System.getenv(LEVEL_ENV) == null);
        System.setProperty("netty.logging.level", "ERROR");
        assertEquals(LogLevel.ERROR, NettyLoggingUtil.getLoggingLevel());
    }

    @Test
    public void testInvalidLoggingLevelFallsBackToInfo() {
        assumeTrue("Env var overrides test", System.getenv(LEVEL_ENV) == null);
        System.setProperty("netty.logging.level", "bogus");
        assertEquals(LogLevel.INFO, NettyLoggingUtil.getLoggingLevel());
    }

    @Test
    public void testDefaultLoggingHandlerName() {
        assumeTrue("Env var overrides test", System.getenv(HANDLER_NAME_ENV) == null);
        assertEquals("logging", NettyLoggingUtil.getLoggingHandlerName());
    }

    @Test
    public void testLoggingHandlerNameViaSystemProperty() {
        assumeTrue("Env var overrides test", System.getenv(HANDLER_NAME_ENV) == null);
        System.setProperty("netty.logging.handler.name", "customLogging");
        assertEquals("customLogging", NettyLoggingUtil.getLoggingHandlerName());
    }
}
