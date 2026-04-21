package com.xinchentechnote.exchange.sse.impl;

import com.finproto.sse.bin.messages.*;
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
                Logon logon = new Logon();
                logon.setSenderCompId("send");
                logon.setTargetCompId("target");
                logon.setQsize(10);
                logon.setHeartBtInt((short) 5);
                logon.setPrtclVersion("1.0");
                logon.setTradeDate(20260420);
                sseTraderApi.ReqLogon(logon);
            }

            @Override
            public void OnFrontDisconnected(int nReason) {

            }

            @Override
            public void OnHeartBeatWarning(int nTimeLapse) {

            }

            @Override
            public void OnLogon(Logon logon) {
                //Logout logout = new Logout();
                //logout.setSessionStatus(1);
                //logout.setText("logout");
                //sseTraderApi.ReqUserLogout(new ReqUserLogoutField(),2);
            }

            @Override
            public void OnLogout(Logout logout) {

            }

            @Override
            public void OnConfirm(Confirm confirm) {

            }

            @Override
            public void OnReport(Report report) {

            }

            @Override
            public void OnCancelReject(CancelReject cancelReject) {

            }

        });
        sseTraderApi.Init();
        LockSupport.parkNanos(10_000_000_000L);
    }
}