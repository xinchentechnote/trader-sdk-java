package com.xinchentechnote.exchange.sse.impl;

import com.finproto.sse.bin.messages.*;
import com.xinchentechnote.exchange.sse.SseTraderApi;
import com.xinchentechnote.exchange.sse.SseTraderSpi;
import com.google.common.io.Resources;
import com.google.common.base.Charsets;

import java.net.URL;
import java.util.List;
import java.util.concurrent.locks.LockSupport;


public class NettySseTraderApiTest {

    @org.junit.Test
    public void init() {
        SseTraderApi sseTraderApi = SseTraderApi.CreateSseTraderApi();
        sseTraderApi.registerFront("tcp://127.0.0.1:9010");
        sseTraderApi.registerSpi(new SseTraderSpi() {
            @Override
            public void onFrontConnected() {
                Logon logon = new Logon();
                logon.setSenderCompId("send");
                logon.setTargetCompId("target");
                logon.setQsize(10);
                logon.setHeartBtInt((short) 5);
                logon.setPrtclVersion("1.0");
                logon.setTradeDate(20260420);
                sseTraderApi.reqLogon(logon);
            }

            @Override
            public void onFrontDisconnected(int nReason) {
                System.out.println("Disconnected, reason: " + nReason);
            }

            @Override
            public void onHeartBeatWarning(int nTimeLapse) {
                System.out.println("Heartbeat warning, time lapse: " + nTimeLapse);
            }

            @Override
            public void onLogon(Logon logon) {
                System.out.println(logon);
                //Logout logout = new Logout();
                //logout.setSessionStatus(1);
                //logout.setText("logout");
                //sseTraderApi.ReqUserLogout(new ReqUserLogoutField(),2);
                try {
                    URL url = getClass().getClassLoader().getResource("sse_58.csv");
                    String csvContent = Resources.toString(url, Charsets.UTF_8);
                    List<NewOrderSingle> newOrderSingles = CsvHelper.parse(csvContent, NewOrderSingle.class);
                    newOrderSingles.forEach(sseTraderApi::reqNewOrderSingle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onLogout(Logout logout) {
                System.out.println(logout);
            }

            @Override
            public void onConfirm(Confirm confirm) {
                System.out.println(confirm);
            }

            @Override
            public void onReport(Report report) {
                System.out.println(report);
            }

            @Override
            public void onCancelReject(CancelReject cancelReject) {
                System.out.println(cancelReject);
            }

        });
        sseTraderApi.init();
        LockSupport.parkNanos(20_000_000_000L);
    }
}