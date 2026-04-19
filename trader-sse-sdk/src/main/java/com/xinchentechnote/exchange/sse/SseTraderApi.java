package com.xinchentechnote.exchange.sse;

import com.xinchentechnote.exchange.sse.dto.*;
import com.xinchentechnote.exchange.sse.impl.NettySseTraderApi;

public interface SseTraderApi {

    static SseTraderApi CreateSseTraderApi(String flowPath){
        return new NettySseTraderApi(flowPath);
    }

    static <T extends SseTraderApi> T create(Class<T> implClass, String flowPath) throws Exception {
        try {
            // 查找带有 String 参数的构造函数
            return implClass.getConstructor(String.class).newInstance(flowPath);
        } catch (NoSuchMethodException e) {
            // 如果没有带参数的构造函数，尝试无参构造函数
            return implClass.getConstructor().newInstance();
        }
    }

    String GetApiVersion();
    void Init();
    void Join();

    int GetTradingDay();
    FrontInfoField GetFrontInfo();

    void RegisterFront(String frontAddress);
    void RegisterNameServer(String nameServerAddress);

    void RegisterSpi(SseTraderSpi spi);

    void ReqUserLogin(ReqUserLoginField reqUserLoginField, int requestId);
    void ReqUserLogout(ReqUserLogoutField reqUserLoginField, int requestId);

    int ReqOrderInsert(InputOrderField inputOrderField, int requestId);
    int ReqOrderAction(InputOrderActionField inputOrderActionField, int requestId);

}
