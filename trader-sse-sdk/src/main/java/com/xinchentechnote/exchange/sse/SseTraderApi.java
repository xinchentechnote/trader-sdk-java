package com.xinchentechnote.exchange.sse;

import com.xinchentechnote.exchange.sse.dto.*;
import com.xinchentechnote.exchange.sse.impl.NettySseTraderApi;

public interface SseTraderApi {

    static SseTraderApi CreateSseTraderApi(){
        return new NettySseTraderApi();
    }

    static <T extends SseTraderApi> T create(Class<T> implClass, Object[] args) throws Exception {
        if (args != null && args.length > 0) {
            // 查找带有指定参数类型的构造函数
            Class<?>[] paramTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                paramTypes[i] = args[i].getClass();
            }
            implClass.getConstructor(paramTypes).newInstance(args);
        }
        return implClass.getConstructor().newInstance();
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
