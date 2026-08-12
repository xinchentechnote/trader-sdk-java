package com.xinchentechnote.exchange.szse.impl;

import com.finproto.codec.BinaryCodec;
import com.finproto.szse.bin.messages.*;
import com.xinchentechnote.exchange.common.ApiStatus;
import com.xinchentechnote.exchange.common.HandlerName;
import com.xinchentechnote.exchange.szse.SzseTraderSpi;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.EventExecutor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SzseApiMessageHandlerTest {

    private static final int MSG_LOGON = 1;
    private static final int MSG_LOGOUT = 2;
    private static final int MSG_HEARTBEAT = 3;
    private static final int MSG_BUSINESS_REJECT = 4;
    private static final int MSG_PLATFORM_STATE_INFO = 6;
    private static final int MSG_REPORT_FINISHED = 7;
    private static final int MSG_TRADING_SESSION_STATUS = 10;
    private static final int MSG_EXECUTION_CONFIRM = 200102;
    private static final int MSG_EXECUTION_REPORT = 200115;
    private static final int MSG_CANCEL_REJECT = 290008;

    private NettySzseTraderApi api;
    private SzseApiMessageHandler handler;

    @Mock
    private SzseTraderSpi spi;
    @Mock
    private ChannelHandlerContext ctx;
    @Mock
    private Channel mockChannel;
    @Mock
    private ChannelPipeline pipeline;
    @Mock
    private EventExecutor executor;

    @Before
    public void setUp() {
        api = new NettySzseTraderApi();
        api.setSpi(spi);
        when(mockChannel.alloc()).thenReturn(UnpooledByteBufAllocator.DEFAULT);
        when(ctx.channel()).thenReturn(mockChannel);
        handler = new SzseApiMessageHandler(api);
    }

    @Test
    public void testChannelActive() throws Exception {
        handler.channelActive(ctx);

        assertEquals(mockChannel, api.getChannel());
        assertEquals(ApiStatus.CONNECTED, api.getStatus());
        verify(spi).onFrontConnected();
    }

    @Test
    public void testChannelInactive() throws Exception {
        api.setStatus(ApiStatus.CONNECTED);
        handler.channelInactive(ctx);

        assertEquals(ApiStatus.DISCONNECTED, api.getStatus());
        verify(spi).onFrontDisconnected(-1);
    }

    @Test
    public void testChannelReadHeartbeatDoesNothing() throws Exception {
        handler.channelRead0(ctx, encodeMessage(MSG_HEARTBEAT, new Heartbeat()));

        verifyNoInteractions(spi);
    }

    @Test
    public void testChannelReadLogon() throws Exception {
        Logon logon = new Logon();
        logon.setHeartBtint(30);
        when(ctx.pipeline()).thenReturn(pipeline);

        handler.channelRead0(ctx, encodeMessage(MSG_LOGON, logon));

        assertEquals(ApiStatus.LOGGED_IN, api.getStatus());
        verify(spi).onLogon(any(Logon.class));
        verify(pipeline).remove(HandlerName.IDLE);
        verify(pipeline).addAfter(eq(HandlerName.FRAME), eq(HandlerName.IDLE), any(IdleStateHandler.class));
    }

    @Test
    public void testChannelReadLogout() throws Exception {
        when(ctx.executor()).thenReturn(executor);

        handler.channelRead0(ctx, encodeMessage(MSG_LOGOUT, new Logout()));

        assertEquals(ApiStatus.LOGGING_OUT, api.getStatus());
        verify(spi).onLogout(any(Logout.class));
        verify(executor).schedule(any(Runnable.class), anyLong(), eq(TimeUnit.SECONDS));
    }

    @Test
    public void testChannelReadExecutionConfirm() throws Exception {
        ExecutionConfirm confirm = new ExecutionConfirm();
        confirm.setApplId("010");
        confirm.setApplExtend(new Extend100101());

        handler.channelRead0(ctx, encodeMessage(MSG_EXECUTION_CONFIRM, confirm));

        verify(spi).onExecutionConfirm(any(ExecutionConfirm.class));
    }

    @Test
    public void testChannelReadExecutionReport() throws Exception {
        ExecutionReport report = new ExecutionReport();
        report.setApplId("010");
        report.setApplExtend(new Extend100101());

        handler.channelRead0(ctx, encodeMessage(MSG_EXECUTION_REPORT, report));

        verify(spi).onExecutionReport(any(ExecutionReport.class));
    }

    @Test
    public void testChannelReadCancelReject() throws Exception {
        handler.channelRead0(ctx, encodeMessage(MSG_CANCEL_REJECT, new CancelReject()));

        verify(spi).onCancelReject(any(CancelReject.class));
    }

    @Test
    public void testChannelReadBusinessReject() throws Exception {
        handler.channelRead0(ctx, encodeMessage(MSG_BUSINESS_REJECT, new BusinessReject()));

        verify(spi).onBusinessReject(any(BusinessReject.class));
    }

    @Test
    public void testChannelReadPlatformStateInfo() throws Exception {
        handler.channelRead0(ctx, encodeMessage(MSG_PLATFORM_STATE_INFO, new PlatformStateInfo()));

        verify(spi).onPlatformStateInfo(any(PlatformStateInfo.class));
    }

    @Test
    public void testChannelReadTradingSessionStatus() throws Exception {
        handler.channelRead0(ctx, encodeMessage(MSG_TRADING_SESSION_STATUS, new TradingSessionStatus()));

        verify(spi).onTradingSessionStatus(any(TradingSessionStatus.class));
    }

    @Test
    public void testChannelReadReportFinished() throws Exception {
        handler.channelRead0(ctx, encodeMessage(MSG_REPORT_FINISHED, new ReportFinished()));

        verify(spi).onReportFinished(any(ReportFinished.class));
    }

    @Test
    public void testUserEventReaderIdleSendsHeartbeatAndClosesAfterThree() throws Exception {
        api.setChannel(mockChannel);

        handler.userEventTriggered(ctx, IdleStateEvent.READER_IDLE_STATE_EVENT);
        handler.userEventTriggered(ctx, IdleStateEvent.READER_IDLE_STATE_EVENT);
        handler.userEventTriggered(ctx, IdleStateEvent.READER_IDLE_STATE_EVENT);

        verify(mockChannel, times(3)).writeAndFlush(any());
        verify(ctx).close();
    }

    @Test
    public void testUserEventWriterIdleDoesNothing() throws Exception {
        api.setChannel(mockChannel);

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
        SzseBinary msg = new SzseBinary();
        msg.setMsgType(msgType);
        msg.setBody(body);
        ByteBuf buf = Unpooled.buffer(256);
        msg.encode(buf);
        return buf;
    }
}
