package com.xinchentechnote.exchange.sse;

import com.finproto.sse.bin.messages.*;
import com.xinchentechnote.exchange.common.ApiStatus;
import com.xinchentechnote.exchange.common.FrontInfoField;
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

    ApiStatus getApiStatus();

    String getApiVersion();
    void init();
    void join();

    int getTradingDay();
    FrontInfoField getFrontInfo();

    void registerFront(String frontAddress);

    void registerSpi(SseTraderSpi spi);

    void reqLogon(Logon logon);
    void reqLogout(Logout logout);

    int reqNewOrderSingle(NewOrderSingle newOrderSingle);
    int reqOrderCancel(OrderCancel orderCancel);

    int reqExecRptSync(ExecRptSync execRptSync);
}
