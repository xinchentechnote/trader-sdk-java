package com.xinchentechnote.exchange.szse;

import com.finproto.szse.bin.messages.Logon;
import com.finproto.szse.bin.messages.Logout;
import com.xinchentechnote.exchange.common.ApiStatus;
import com.xinchentechnote.exchange.common.FrontInfoField;
import com.xinchentechnote.exchange.szse.impl.NettySzseTraderApi;

public interface SzseTraderApi {

    static SzseTraderApi CreateSzseTraderApi() {
        return new NettySzseTraderApi();
    }

    ApiStatus getApiStatus();

    String getApiVersion();
    void init();
    void join();

    int getTradingDay();
    FrontInfoField getFrontInfo();

    void registerFront(String frontAddress);

    void registerSpi(SzseTraderSpi spi);

    void reqLogon(Logon logon);
    void reqLogout(Logout logout);

}
