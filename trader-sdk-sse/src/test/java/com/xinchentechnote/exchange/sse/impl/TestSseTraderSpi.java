package com.xinchentechnote.exchange.sse.impl;

import com.finproto.sse.bin.messages.*;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.xinchentechnote.exchange.common.ApiLogLevel;
import com.xinchentechnote.exchange.common.utils.CsvHelper;
import com.xinchentechnote.exchange.sse.SseTraderApi;
import com.xinchentechnote.exchange.sse.SseTraderSpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;


class TestSseTraderSpi implements SseTraderSpi {

    private static final Logger logger = LoggerFactory.getLogger(TestSseTraderSpi.class);

    private final SseTraderApi sseTraderApi;

    public TestSseTraderSpi(SseTraderApi sseTraderApi) {
        this.sseTraderApi = sseTraderApi;
    }

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
        logger.info("Disconnected, reason: " + nReason);
    }

    @Override
    public void onHeartBeatWarning(int nTimeLapse) {
        logger.info("Heartbeat warning, time lapse: " + nTimeLapse);
    }

    @Override
    public void onLogon(Logon logon) {
        logger.info("{}", logon);
        try {
            URL url = getClass().getClassLoader().getResource("sse_58.csv");
            String csvContent = Resources.toString(url, Charsets.UTF_8);
            List<NewOrderSingle> newOrderSingles = CsvHelper.parse(csvContent, NewOrderSingle.class);
            newOrderSingles.forEach(sseTraderApi::reqNewOrderSingle);
        } catch (Exception e) {
            logger.error("Failed to read CSV file", e);
        }
    }

    @Override
    public void onLogout(Logout logout) {
        logger.info("{}", logout);
    }

    @Override
    public void onConfirm(Confirm confirm) {
        logger.info("{}", confirm);
    }

    @Override
    public void onOrderReject(OrderReject orderReject) {
        logger.info("{}", orderReject);
    }

    @Override
    public void onReport(Report report) {
        logger.info("{}", report);
    }

    @Override
    public void onCancelReject(CancelReject cancelReject) {
        logger.info("{}", cancelReject);
    }

    @Override
    public void onExecRptInfo(ExecRptInfo execRptInfo) {
        logger.info("{}", execRptInfo);
    }

    @Override
    public void onPlatformState(PlatformState platformState) {
        logger.info("{}", platformState);
    }

    @Override
    public void onExecRptSyncRsp(ExecRptSyncRsp execRptSyncRsp) {

    }

    @Override
    public void onExecRptEndOfStream(ExecRptEndOfStream execRptEndOfStream) {

    }

    @Override
    public void onLog(ApiLogLevel level, String message) {

    }

}
