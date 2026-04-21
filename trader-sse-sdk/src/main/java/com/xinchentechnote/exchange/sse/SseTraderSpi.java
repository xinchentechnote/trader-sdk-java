package com.xinchentechnote.exchange.sse;

import com.finproto.sse.bin.messages.*;

public interface SseTraderSpi {

    void OnFrontConnected();
    void OnFrontDisconnected(int nReason);
    void OnHeartBeatWarning(int nTimeLapse);

    void OnLogon(Logon logon);
    void OnLogout(Logout logout);

    void OnConfirm(Confirm confirm);
    void OnReport(Report report);
    void OnCancelReject(CancelReject cancelReject);
}
