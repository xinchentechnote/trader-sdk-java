package com.xinchentechnote.exchange.sse.impl;

import com.xinchentechnote.exchange.sse.SseTraderApi;

import java.util.concurrent.locks.LockSupport;

import static org.junit.Assert.*;

public class NettySseTraderApiTest {

    @org.junit.Test
    public void init() {
        SseTraderApi sseTraderApi = SseTraderApi.CreateSseTraderApi("");
        sseTraderApi.RegisterFront("tcp://127.0.0.1:9010");
        sseTraderApi.Init();
        LockSupport.parkNanos(1000_000_000L);
    }
}