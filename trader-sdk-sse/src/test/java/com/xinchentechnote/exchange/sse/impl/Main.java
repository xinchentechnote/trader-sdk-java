package com.xinchentechnote.exchange.sse.impl;

import com.xinchentechnote.exchange.sse.SseTraderApi;

public class Main {
    public static void main(String[] args) {
        SseTraderApi sseTraderApi = SseTraderApi.CreateSseTraderApi();
        sseTraderApi.registerFront("tcp://127.0.0.1:9010");
        sseTraderApi.registerSpi(new TestSseTraderSpi(sseTraderApi));
        sseTraderApi.init();
    }

}
