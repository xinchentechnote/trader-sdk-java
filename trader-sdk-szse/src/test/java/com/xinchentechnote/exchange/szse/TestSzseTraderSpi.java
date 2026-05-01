package com.xinchentechnote.exchange.szse;

import com.finproto.szse.bin.messages.*;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.xinchentechnote.exchange.common.ApiLogLevel;
import com.xinchentechnote.exchange.common.utils.CsvHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;

public class TestSzseTraderSpi implements SzseTraderSpi {

    private static Logger logger = LoggerFactory.getLogger(TestSzseTraderSpi.class);
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
    public void onLogon(Logon logon) {
        logger.info("{}", logon);
        try {
            URL url = getClass().getClassLoader().getResource("szse_100101.csv");
            String csvContent = Resources.toString(url, Charsets.UTF_8);
            List<NewOrder> newOrderSingles = CsvHelper.parse(csvContent, NewOrder.class);
            newOrderSingles.forEach(order->order.setApplExtend(new Extend100101()));
            newOrderSingles.forEach(sseTraderApi::reqNewOrder);
        } catch (Exception e) {
            logger.error("Failed to read CSV file", e);
        }
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

    @Override
    public void onPlatformStateInfo(PlatformStateInfo platformStateInfo) {

    }

    @Override
    public void onPlatformInfo(PlatformInfo platformInfo) {

    }

    @Override
    public void onTradingSessionStatus(TradingSessionStatus tradingSessionStatus) {

    }

    @Override
    public void onReportFinished(ReportFinished reportFinished) {

    }
}
