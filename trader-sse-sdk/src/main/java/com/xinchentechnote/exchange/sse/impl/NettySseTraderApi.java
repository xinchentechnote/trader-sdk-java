package com.xinchentechnote.exchange.sse.impl;

import com.xinchentechnote.exchange.sse.SseTraderApi;
import com.xinchentechnote.exchange.sse.SseTraderSpi;
import com.xinchentechnote.exchange.sse.dto.*;

public class NettySseTraderApi implements SseTraderApi {

    private String flowPath;

    public NettySseTraderApi(String flowPath) {
        this.flowPath = flowPath;
    }

    @Override
    public String GetApiVersion() {
        return "";
    }

    @Override
    public void Init() {

    }

    @Override
    public void Join() {

    }

    @Override
    public int GetTradingDay() {
        return 0;
    }

    @Override
    public FrontInfoField GetFrontInfo() {
        return null;
    }

    @Override
    public void RegisterFront(String frontAddress) {

    }

    @Override
    public void RegisterNameServer(String nameServerAddress) {

    }

    @Override
    public void RegisterSpi(SseTraderSpi spi) {

    }

    @Override
    public void ReqUserLogin(ReqUserLoginField reqUserLoginField, int requestId) {

    }

    @Override
    public void ReqUserLogout(ReqUserLogoutField reqUserLoginField, int requestId) {

    }

    @Override
    public int ReqOrderInsert(InputOrderField inputOrderField, int requestId) {
        return 0;
    }

    @Override
    public int ReqOrderAction(InputOrderActionField inputOrderActionField, int requestId) {
        return 0;
    }
}
