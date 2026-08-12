package com.xinchentechnote.exchange.szse.impl;

import com.finproto.szse.bin.messages.*;
import com.xinchentechnote.exchange.common.ApiStatus;
import com.xinchentechnote.exchange.common.FrontInfoField;
import com.xinchentechnote.exchange.szse.SzseTraderSpi;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NettySzseTraderApiTest {

    private NettySzseTraderApi api;

    @Mock
    private SzseTraderSpi mockSpi;

    @Mock
    private Channel mockChannel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        api = new NettySzseTraderApi();
        when(mockChannel.alloc()).thenReturn(UnpooledByteBufAllocator.DEFAULT);
    }

    @Test
    public void testConstructor() {
        assertEquals(ApiStatus.NEW, api.getStatus());
        assertEquals("1.0.0", api.getApiVersion());
        assertEquals(0, api.getTradingDay());
    }

    @Test
    public void testRegisterFront() {
        api.registerFront("tcp://127.0.0.1:9011");
        FrontInfoField frontInfo = api.getFrontInfo();
        assertNotNull(frontInfo);
        assertEquals("127.0.0.1", frontInfo.getIp());
        assertEquals(9011, frontInfo.getPort());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterFrontWithNull() {
        api.registerFront(null);
    }

    @Test
    public void testRegisterSpi() {
        api.setSpi(mockSpi);
        assertEquals(mockSpi, api.getSpi());
    }

    @Test
    public void testReqLogon() {
        api.setChannel(mockChannel);
        when(mockChannel.isActive()).thenReturn(true);

        api.reqLogon(new Logon());

        verify(mockChannel).writeAndFlush(any());
    }

    @Test
    public void testReqLogout() {
        api.setChannel(mockChannel);
        when(mockChannel.isActive()).thenReturn(true);

        api.reqLogout(new Logout());

        verify(mockChannel).writeAndFlush(any());
    }

    @Test
    public void testReqNewOrder() {
        api.setChannel(mockChannel);
        when(mockChannel.isActive()).thenReturn(true);

        api.reqNewOrder(new NewOrder());

        verify(mockChannel).writeAndFlush(any());
    }

    @Test
    public void testReqOrderCancelRequest() {
        api.setChannel(mockChannel);
        when(mockChannel.isActive()).thenReturn(true);

        api.reqOrderCancelRequest(new OrderCancelRequest());

        verify(mockChannel).writeAndFlush(any());
    }

    @Test
    public void testSendHeartbeat() {
        api.setChannel(mockChannel);

        api.sendHeartbeat();

        verify(mockChannel).writeAndFlush(any());
    }

    @Test(expected = IllegalStateException.class)
    public void testSendMessageWhenChannelInactive() {
        api.setChannel(mockChannel);
        when(mockChannel.isActive()).thenReturn(false);

        api.reqLogon(new Logon());
    }

    @Test(expected = IllegalStateException.class)
    public void testSendMessageWhenChannelNull() {
        api.reqLogon(new Logon());
    }
}
