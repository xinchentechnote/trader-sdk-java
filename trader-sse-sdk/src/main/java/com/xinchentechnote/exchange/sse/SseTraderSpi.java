package com.xinchentechnote.exchange.sse;

import com.finproto.sse.bin.messages.*;

public interface SseTraderSpi {

    void onFrontConnected();
    void onFrontDisconnected(int nReason);
    void onHeartBeatWarning(int nTimeLapse);

    void onLogon(Logon logon);
    void onLogout(Logout logout);

    void onConfirm(Confirm confirm);
    void onReport(Report report);
    void onCancelReject(CancelReject cancelReject);
}
