package com.xinchentechnote.exchange.sse;

import com.xinchentechnote.exchange.sse.dto.*;

public interface SseTraderSpi {

    void OnFrontConnected();
    void OnFrontDisconnected(int nReason);
    void OnHeartBeatWarning(int nTimeLapse);

    void OnRspUserLogin(RspUserLoginField pRspUserLoginField, RspInfoField pRspInfo, int nRequestID, boolean bIsLast);
    void OnRspUserLogout(RspInfoField pRspInfo, int nRequestID, boolean bIsLast);

    void OnRspOrderInsert(InputOrderField inputOrderField, RspInfoField pRspInfo, int nRequestID, boolean bIsLast);
    void OnRspOrderAction(InputOrderActionField inputOrderActionField, RspInfoField pRspInfo, int nRequestID, boolean bIsLast);
    void OnRtnOrder(OrderField order);
    void OnRtnTrade(TradeField trade);
}
