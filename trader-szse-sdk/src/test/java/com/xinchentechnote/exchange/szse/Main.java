package com.xinchentechnote.exchange.szse;

public class Main {
    public static void main(String[] args) {
        SzseTraderApi sseTraderApi = SzseTraderApi.CreateSzseTraderApi();
        sseTraderApi.registerFront("tcp://127.0.0.1:9011");
        sseTraderApi.registerSpi(new TestSzseTraderSpi(sseTraderApi));
        sseTraderApi.init();
    }
}
