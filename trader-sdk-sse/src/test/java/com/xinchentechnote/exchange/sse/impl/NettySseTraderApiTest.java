package com.xinchentechnote.exchange.sse.impl;

import com.finproto.sse.bin.messages.*;
import com.xinchentechnote.exchange.common.ApiStatus;
import com.xinchentechnote.exchange.common.FrontInfoField;
import com.xinchentechnote.exchange.common.utils.CsvHelper;
import com.xinchentechnote.exchange.sse.SseTraderSpi;
import com.google.common.io.Resources;
import com.google.common.base.Charsets;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import org.junit.After;
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
        when(mockChannel.alloc()).thenReturn(UnpooledByteBufAllocator.DEFAULT);
    }

    @After
    public void tearDown() {
        api.shutdown();
    }

    @Test
    public void testConstructor() {
        NettySseTraderApi newApi = new NettySseTraderApi();
        try {
            assertEquals(ApiStatus.NEW, newApi.getStatus().get());
            assertEquals("1.0.0", newApi.getApiVersion());
            assertEquals(20260421, newApi.getTradingDay());
        } finally {
            newApi.shutdown();
        }
    }

    @Test
    public void testRegisterFront() {
        String frontAddress = "tcp://127.0.0.1:9010";
        api.registerFront(frontAddress);
        FrontInfoField frontInfo = api.getFrontInfo();
        assertNotNull(frontInfo);
        assertEquals("tcp", frontInfo.getProtocol());
        assertEquals("127.0.0.1", frontInfo.getIp());
        assertEquals(9010, frontInfo.getPort());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterFrontWithInvalidProtocol() {
        api.registerFront("http://127.0.0.1:9010");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterFrontWithNull() {
        api.registerFront(null);
    }

    @Test
    public void testRegisterSpi() {
        api.registerSpi(mockSpi);
        assertEquals(mockSpi, api.getSpi());
    }

    @Test(expected = IllegalStateException.class)
    public void testInitWithoutFrontThrows() {
        api.init();
    }

    @Test
    public void testReqLogonWhenNotConnected() {
        Logon logon = new Logon();
        api.reqLogon(logon);
        verifyNoInteractions(mockChannel);
        assertEquals(ApiStatus.NEW, api.getStatus().get());
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
    public void testReqLogoutWhenNotLoggedIn() {
        api.getStatus().set(ApiStatus.CONNECTED);

        api.reqLogout(new Logout());

        assertEquals(ApiStatus.CONNECTED, api.getStatus().get());
        assertNull(api.getChannel());
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

    @Test
    public void testReqNewOrderSingleWhenNotLoggedIn() {
        int result = api.reqNewOrderSingle(new NewOrderSingle());
        assertEquals(-1, result);
        verifyNoInteractions(mockChannel);
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
    public void testReqOrderCancelWhenNotLoggedIn() {
        int result = api.reqOrderCancel(new OrderCancel());
        assertEquals(-1, result);
        verifyNoInteractions(mockChannel);
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
    public void testReqExecRptSyncWhenNotLoggedIn() {
        int result = api.reqExecRptSync(new ExecRptSync());
        assertEquals(-1, result);
        verifyNoInteractions(mockChannel);
    }

    @Test
    public void testReqExecRptSync() {
        api.getStatus().set(ApiStatus.LOGGED_IN);
        api.setChannel(mockChannel);
        when(mockChannel.isActive()).thenReturn(true);

        int result = api.reqExecRptSync(new ExecRptSync());
        assertEquals(0, result);
        verify(mockChannel).writeAndFlush(any());
    }

    @Test
    public void testSendHeartbeat() {
        api.setChannel(mockChannel);
        when(mockChannel.isActive()).thenReturn(true);

        api.sendHeartbeat();

        verify(mockChannel).writeAndFlush(any());
    }

    @Test(expected = IllegalStateException.class)
    public void testSendMessageWhenChannelInactive() {
        api.getStatus().set(ApiStatus.CONNECTED);
        api.setChannel(mockChannel);
        when(mockChannel.isActive()).thenReturn(false);

        api.reqLogon(new Logon());
    }

    @Test
    public void testCsvParsing() throws Exception {
        URL url = getClass().getClassLoader().getResource("sse_58.csv");
        String csvContent = Resources.toString(url, Charsets.UTF_8);
        List<NewOrderSingle> orders = CsvHelper.parse(csvContent, NewOrderSingle.class);
        assertEquals(2, orders.size());
        assertEquals("c10001", orders.get(0).getClOrdId());
        assertEquals("600000", orders.get(0).getSecurityId());
        assertEquals(100, orders.get(0).getOrderQty());
        assertEquals(10, orders.get(0).getPrice());
        assertEquals("c10002", orders.get(1).getClOrdId());
        assertEquals(10, orders.get(1).getBizId());
        assertEquals("2", orders.get(1).getSide());
    }
}
