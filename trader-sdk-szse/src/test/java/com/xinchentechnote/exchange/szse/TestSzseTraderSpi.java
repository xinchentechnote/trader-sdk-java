package com.xinchentechnote.exchange.szse;

import com.finproto.szse.bin.messages.*;
import com.xinchentechnote.exchange.common.ApiLogLevel;

public class TestSzseTraderSpi implements SzseTraderSpi {
    private SzseTraderApi sseTraderApi;

    public TestSzseTraderSpi(SzseTraderApi sseTraderApi) {
        this.sseTraderApi = sseTraderApi;
    }

    @Override
    public void onFrontConnected() {
        Logon logon = new Logon();
        logon.setPassword("123456");
        logon.setTargetCompId("002");
        logon.setSenderCompId("001");
        logon.setHeartBtint(30);
        logon.setDefaultApplVerId("0");
        sseTraderApi.reqLogon(logon);
    }

    @Override
    public void onFrontDisconnected(int nReason) {

    }

    @Override
    public void onHeartBeatWarning(int nTimeLapse) {

    }

    @Override
    public void onLogon(Logon logon) {

    }

    @Override
    public void onLogout(Logout logout) {

    }

    @Override
    public void onLog(ApiLogLevel level, String message) {

    }

    @Override
    public void onExecutionConfirm(ExecutionConfirm executionConfirm) {

    }

    @Override
    public void onExecutionReport(ExecutionReport executionReport) {

    }

    @Override
    public void onCancelReject(CancelReject cancelReject) {

    }

    @Override
    public void onBusinessReject(BusinessReject businessReject) {

    }
}
