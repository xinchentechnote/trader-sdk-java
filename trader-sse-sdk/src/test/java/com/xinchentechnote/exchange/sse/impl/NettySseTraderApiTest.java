package com.xinchentechnote.exchange.sse.impl;

import com.xinchentechnote.exchange.sse.SseTraderApi;
import com.xinchentechnote.exchange.sse.SseTraderSpi;
import com.xinchentechnote.exchange.sse.dto.*;

import java.util.concurrent.locks.LockSupport;


public class NettySseTraderApiTest {

    @org.junit.Test
    public void init() {
        SseTraderApi sseTraderApi = SseTraderApi.CreateSseTraderApi();
        sseTraderApi.RegisterFront("tcp://127.0.0.1:9010");
        sseTraderApi.RegisterSpi(new SseTraderSpi() {
            @Override
            public void OnFrontConnected() {
                sseTraderApi.ReqUserLogin(new ReqUserLoginField(),1);
            }

            @Override
            public void OnFrontDisconnected(int nReason) {

            }

            @Override
            public void OnHeartBeatWarning(int nTimeLapse) {

            }

            @Override
            public void OnRspUserLogin(RspUserLoginField pRspUserLoginField, RspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
                sseTraderApi.ReqUserLogout(new ReqUserLogoutField(),2);
            }

            @Override
            public void OnRspUserLogout(RspInfoField pRspInfo, int nRequestID, boolean bIsLast) {

            }

            @Override
            public void OnRspOrderInsert(InputOrderField inputOrderField, RspInfoField pRspInfo, int nRequestID, boolean bIsLast) {

            }

            @Override
            public void OnRspOrderAction(InputOrderActionField inputOrderActionField, RspInfoField pRspInfo, int nRequestID, boolean bIsLast) {

            }

            @Override
            public void OnRtnOrder(OrderField order) {

            }

            @Override
            public void OnRtnTrade(TradeField trade) {

            }
        });
        sseTraderApi.Init();
        LockSupport.parkNanos(10_000_000_000L);
    }
}