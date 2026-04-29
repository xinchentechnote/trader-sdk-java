package com.xinchentechnote.exchange.szse;

import com.finproto.szse.bin.messages.Logon;
import com.finproto.szse.bin.messages.Logout;

public class TestSzseTraderSpi implements SzseTraderSpi {
    public TestSzseTraderSpi(SzseTraderApi sseTraderApi) {
    }

    @Override
    public void onFrontConnected() {

    }

    @Override
    public void onFrontDisconnected(int nReason) {

    }

    @Override
    public void onHeartBeatWarning(int nTimeLapse) {

    }

    @Override
    public void onLogon(Logon logon) {

    }

    @Override
    public void onLogout(Logout logout) {

    }
}
