package com.xinchentechnote.exchange.szse.impl;

import com.finproto.codec.BinaryCodec;
import com.finproto.szse.bin.messages.*;
import com.xinchentechnote.exchange.common.ApiStatus;
import com.xinchentechnote.exchange.common.HandlerName;
import com.xinchentechnote.exchange.szse.SzseTraderSpi;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class SzseApiMessageHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static Logger logger = LoggerFactory.getLogger(SzseApiMessageHandler.class);

    private NettySzseTraderApi szseTraderApi;
    private SzseTraderSpi spi;
    private int heartbeatTimeoutCounter = 0;

    public SzseApiMessageHandler(NettySzseTraderApi szseTraderApi) {
        this.szseTraderApi = szseTraderApi;
        this.spi = szseTraderApi.getSpi();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.szseTraderApi.setChannel(ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        heartbeatTimeoutCounter = 0;
        SzseBinary msg = new SzseBinary();
        msg.decode(byteBuf);
        System.out.println("Received message: " + msg);
        BinaryCodec body = msg.getBody();
        if (body instanceof Logon) {
            Logon logon = (Logon) body;
            int heartBtint = logon.getHeartBtint();
            szseTraderApi.setStatus(ApiStatus.LOGGED_IN);
            if (null != spi) {
                spi.onLogon(logon);
            }
            ChannelPipeline pipeline = ctx.pipeline();
            pipeline.remove(HandlerName.IDLE);
            pipeline.addAfter(HandlerName.FRAME, HandlerName.IDLE, new IdleStateHandler(heartBtint, 0, 0));
        } else if (body instanceof Logout) {
            // Do nothing, just reset the idle timer
            szseTraderApi.setStatus(ApiStatus.LOGGING_OUT);
        } else if (body instanceof ExecutionConfirm) {
            if (null != spi) {
                spi.onExecutionConfirm((ExecutionConfirm) body);
            }
        } else if (body instanceof ExecutionReport) {
            if (null != spi) {
                spi.onExecutionReport((ExecutionReport) body);
            }
        } else if (body instanceof CancelReject) {
            if (null != spi) {
                spi.onCancelReject((CancelReject) body);
            }
        } else if (body instanceof BusinessReject) {
            if (null != spi) {
                spi.onBusinessReject((BusinessReject) body);
            }
        } else if (body instanceof PlatformStateInfo) {
            if (null != spi) {
                spi.onPlatformStateInfo((PlatformStateInfo) body);
            }
        } else if (body instanceof PlatformInfo) {
            if (null != spi) {
                spi.onPlatformInfo((PlatformInfo) body);
            }
        } else if (body instanceof TradingSessionStatus) {
            if (null != spi) {
                spi.onTradingSessionStatus((TradingSessionStatus) body);
            }
        } else if (body instanceof ReportFinished) {
            if (null != spi) {
                spi.onReportFinished((ReportFinished) body);
            }

        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state() == IdleState.READER_IDLE) {
                heartbeatTimeoutCounter++;
                szseTraderApi.sendHeartbeat();
                if (heartbeatTimeoutCounter >= 3) {
                    logger.warn("Heartbeat timeout, closing connection");
                    ctx.close();
                }
            }
        }
        super.userEventTriggered(ctx, evt);
    }
}
