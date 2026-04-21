package com.xinchentechnote.exchange.sse.impl;

import com.finproto.sse.bin.messages.*;
import com.xinchentechnote.exchange.sse.SseTraderSpi;
import com.google.common.io.Resources;
import com.google.common.base.Charsets;
import io.netty.channel.Channel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NettySseTraderApiTest {

    private NettySseTraderApi api;

    @Mock
    private SseTraderSpi mockSpi;

    @Mock
    private Channel mockChannel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        api = new NettySseTraderApi();
    }

    @Test
    public void testConstructor() {
        NettySseTraderApi newApi = new NettySseTraderApi();
        assertEquals(ApiStatus.NEW, newApi.getStatus().get());
    }

    @Test
    public void testRegisterFront() {
        String frontAddress = "tcp://127.0.0.1:9010";
        api.registerFront(frontAddress);
        FrontInfoField frontInfo = api.getFrontInfo();
        assertNotNull(frontInfo);
        assertEquals("127.0.0.1", frontInfo.getIp());
        assertEquals(9010, frontInfo.getPort());
    }

    @Test
    public void testRegisterSpi() {
        api.registerSpi(mockSpi);
        assertEquals(mockSpi, api.getSpi());
    }

    @Test
    public void testReqLogonWhenNotConnected() {
        Logon logon = new Logon();
        api.reqLogon(logon);
        // Since status is NEW, should not send
        verifyNoInteractions(mockChannel);
    }

    @Test
    public void testReqLogonWhenConnected() {
        // Set status to CONNECTED and mock channel
        api.getStatus().set(ApiStatus.CONNECTED);
        api.setChannel(mockChannel);
        when(mockChannel.isActive()).thenReturn(true);

        Logon logon = new Logon();
        api.reqLogon(logon);

        // Verify that writeAndFlush was called
        verify(mockChannel).writeAndFlush(any());
    }

    @Test
    public void testCsvParsing() throws Exception {
        URL url = getClass().getClassLoader().getResource("sse_58.csv");
        String csvContent = Resources.toString(url, Charsets.UTF_8);
        List<NewOrderSingle> orders = CsvHelper.parse(csvContent, NewOrderSingle.class);
        assertEquals(2, orders.size());
        // Assuming the objects are parsed correctly, further assertions can be added if getters are available
    }

    @Test
    public void testReqNewOrderSingle() {
        api.getStatus().set(ApiStatus.LOGGED_IN);
        api.setChannel(mockChannel);
        when(mockChannel.isActive()).thenReturn(true);

        NewOrderSingle order = new NewOrderSingle();
        int result = api.reqNewOrderSingle(order);
        assertEquals(0, result);
        verify(mockChannel).writeAndFlush(any());
    }

    @Test
    public void testReqOrderCancel() {
        api.getStatus().set(ApiStatus.LOGGED_IN);
        api.setChannel(mockChannel);
        when(mockChannel.isActive()).thenReturn(true);

        OrderCancel cancel = new OrderCancel();
        int result = api.reqOrderCancel(cancel);
        assertEquals(0, result);
        verify(mockChannel).writeAndFlush(any());
    }

    @Test
    public void testReqLogout() {
        api.getStatus().set(ApiStatus.LOGGED_IN);
        api.setChannel(mockChannel);
        when(mockChannel.isActive()).thenReturn(true);

        Logout logout = new Logout();
        api.reqLogout(logout);
        assertEquals(ApiStatus.LOGGING_OUT, api.getStatus().get());
        verify(mockChannel).writeAndFlush(any());
    }

    // Note: init() test is complex due to Netty, skipped for now
}