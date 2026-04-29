package com.xinchentechnote.exchange.szse;

import com.finproto.szse.bin.messages.Logon;
import com.finproto.szse.bin.messages.Logout;

public interface SzseTraderSpi {

    void onFrontConnected();
    void onFrontDisconnected(int nReason);
    void onHeartBeatWarning(int nTimeLapse);

    void onLogon(Logon logon);
    void onLogout(Logout logout);

}
