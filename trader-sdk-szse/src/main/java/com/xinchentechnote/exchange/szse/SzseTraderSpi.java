package com.xinchentechnote.exchange.szse;

import com.finproto.szse.bin.messages.*;
import com.xinchentechnote.exchange.common.ApiLogLevel;

public interface SzseTraderSpi {

    void onFrontConnected();

    void onFrontDisconnected(int nReason);

    void onHeartBeatWarning(int nTimeLapse);

    void onLogon(Logon logon);

    void onLogout(Logout logout);

    void onLog(ApiLogLevel level, String message);

    void onExecutionConfirm(ExecutionConfirm executionConfirm);

    void onExecutionReport(ExecutionReport executionReport);

    void onCancelReject(CancelReject cancelReject);

    void onBusinessReject(BusinessReject businessReject);
}
