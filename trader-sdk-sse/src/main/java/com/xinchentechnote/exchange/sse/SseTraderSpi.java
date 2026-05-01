package com.xinchentechnote.exchange.sse;

import com.finproto.sse.bin.messages.*;
import com.xinchentechnote.exchange.common.ApiLogLevel;

public interface SseTraderSpi {

    void onFrontConnected();

    void onFrontDisconnected(int nReason);

    void onHeartBeatWarning(int nTimeLapse);

    void onLogon(Logon logon);

    void onLogout(Logout logout);

    void onConfirm(Confirm confirm);

    void onOrderReject(OrderReject orderReject);

    void onReport(Report report);

    void onCancelReject(CancelReject cancelReject);

    void onExecRptInfo(ExecRptInfo execRptInfo);

    void onPlatformState(PlatformState platformState);

    void onExecRptSyncRsp(ExecRptSyncRsp execRptSyncRsp);

    void onExecRptEndOfStream(ExecRptEndOfStream execRptEndOfStream);

    void onLog(ApiLogLevel level, String message);
}
