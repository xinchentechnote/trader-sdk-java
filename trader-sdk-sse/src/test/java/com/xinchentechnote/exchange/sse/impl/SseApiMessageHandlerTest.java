package com.xinchentechnote.exchange.sse.impl;

import com.finproto.codec.BinaryCodec;
import com.finproto.sse.bin.messages.*;
import com.xinchentechnote.exchange.common.ApiStatus;
import com.xinchentechnote.exchange.common.HandlerName;
import com.xinchentechnote.exchange.sse.SseTraderSpi;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SseApiMessageHandlerTest {

    private NettySseTraderApi api;
    private SseApiMessageHandler handler;

    @Mock
    private SseTraderSpi spi;
    @Mock
    private ChannelHandlerContext ctx;
    @Mock
    private Channel mockChannel;
    @Mock
    private ChannelPipeline pipeline;

    @Before
    public void setUp() {
        api = new NettySseTraderApi();
        api.registerSpi(spi);
        when(mockChannel.alloc()).thenReturn(UnpooledByteBufAllocator.DEFAULT);
        when(ctx.channel()).thenReturn(mockChannel);
        handler = new SseApiMessageHandler(api);
    }

    @After
    public void tearDown() {
        api.shutdown();
    }

    @Test
    public void testChannelActive() throws Exception {
        handler.channelActive(ctx);

        assertEquals(mockChannel, api.getChannel());
        assertEquals(ApiStatus.CONNECTED, api.getStatus().get());
        verify(spi).onFrontConnected();
    }

    @Test
    public void testChannelInactive() throws Exception {
        api.getStatus().set(ApiStatus.CONNECTED);
        handler.channelInactive(ctx);

        assertEquals(ApiStatus.DISCONNECTED, api.getStatus().get());
        verify(spi).onFrontDisconnected(0);
    }

    @Test
    public void testChannelReadHeartbeat() throws Exception {
        handler.channelRead0(ctx, encodeMessage(SseBinary.BodyMessageFactory.MessageType.HEARTBEAT.getValue(), new Heartbeat()));

        verifyNoInteractions(spi);
    }

    @Test
    public void testChannelReadLogonWithMinHeartBeat() throws Exception {
        Logon logon = new Logon();
        logon.setHeartBtInt((short) HeartBtIntUtil.MIN);

        handler.channelRead0(ctx, encodeMessage(SseBinary.BodyMessageFactory.MessageType.LOGON.getValue(), logon));

        assertEquals(ApiStatus.LOGGED_IN, api.getStatus().get());
        ArgumentCaptor<Logon> captor = ArgumentCaptor.forClass(Logon.class);
        verify(spi).onLogon(captor.capture());
        assertEquals(HeartBtIntUtil.MIN, captor.getValue().getHeartBtInt());
        verify(ctx, never()).pipeline();
    }

    @Test
    public void testChannelReadLogonWithCustomHeartBeat() throws Exception {
        Logon logon = new Logon();
        logon.setHeartBtInt((short) 30);
        when(ctx.pipeline()).thenReturn(pipeline);

        handler.channelRead0(ctx, encodeMessage(SseBinary.BodyMessageFactory.MessageType.LOGON.getValue(), logon));

        assertEquals(ApiStatus.LOGGED_IN, api.getStatus().get());
        ArgumentCaptor<Logon> captor = ArgumentCaptor.forClass(Logon.class);
        verify(spi).onLogon(captor.capture());
        assertEquals(30, captor.getValue().getHeartBtInt());
        verify(pipeline).remove(HandlerName.IDLE);
        verify(pipeline).addAfter(eq(HandlerName.FRAME), eq(HandlerName.IDLE), any(IdleStateHandler.class));
    }

    @Test
    public void testChannelReadLogout() throws Exception {
        Logout logout = new Logout();

        handler.channelRead0(ctx, encodeMessage(SseBinary.BodyMessageFactory.MessageType.LOGOUT.getValue(), logout));

        assertEquals(ApiStatus.DISCONNECTED, api.getStatus().get());
        verify(spi).onLogout(any(Logout.class));
    }

    @Test
    public void testChannelReadConfirm() throws Exception {
        Confirm confirm = new Confirm();

        handler.channelRead0(ctx, encodeMessage(SseBinary.BodyMessageFactory.MessageType.CONFIRM.getValue(), confirm));

        verify(spi).onConfirm(any(Confirm.class));
    }

    @Test
    public void testChannelReadCancelReject() throws Exception {
        CancelReject cancelReject = new CancelReject();

        handler.channelRead0(ctx, encodeMessage(SseBinary.BodyMessageFactory.MessageType.CANCEL_REJECT.getValue(), cancelReject));

        verify(spi).onCancelReject(any(CancelReject.class));
    }

    @Test
    public void testChannelReadReport() throws Exception {
        Report report = new Report();

        handler.channelRead0(ctx, encodeMessage(SseBinary.BodyMessageFactory.MessageType.REPORT.getValue(), report));

        verify(spi).onReport(any(Report.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChannelReadUnknownTypeThrows() throws Exception {
        handler.channelRead0(ctx, encodeMessage(999, null));
    }

    @Test
    public void testUserEventReaderIdleSendsHeartbeatAndClosesAfterThree() throws Exception {
        api.setChannel(mockChannel);
        when(mockChannel.isActive()).thenReturn(true);

        handler.userEventTriggered(ctx, IdleStateEvent.READER_IDLE_STATE_EVENT);
        handler.userEventTriggered(ctx, IdleStateEvent.READER_IDLE_STATE_EVENT);
        handler.userEventTriggered(ctx, IdleStateEvent.READER_IDLE_STATE_EVENT);

        verify(mockChannel, times(3)).writeAndFlush(any());
        verify(ctx).close();
    }

    @Test
    public void testUserEventWriterIdleDoesNothing() throws Exception {
        api.setChannel(mockChannel);
        when(mockChannel.isActive()).thenReturn(true);

        handler.userEventTriggered(ctx, IdleStateEvent.WRITER_IDLE_STATE_EVENT);

        verify(mockChannel, never()).writeAndFlush(any());
        verify(ctx, never()).close();
    }

    @Test
    public void testUserEventNonIdleStateDoesNothing() throws Exception {
        handler.userEventTriggered(ctx, "not-an-idle-event");

        verify(mockChannel, never()).writeAndFlush(any());
        verify(ctx, never()).close();
    }

    private ByteBuf encodeMessage(int msgType, BinaryCodec body) {
        SseBinary msg = new SseBinary();
        msg.setMsgType(msgType);
        msg.setBody(body);
        ByteBuf buf = Unpooled.buffer(256);
        msg.encode(buf);
        return buf;
    }
}
