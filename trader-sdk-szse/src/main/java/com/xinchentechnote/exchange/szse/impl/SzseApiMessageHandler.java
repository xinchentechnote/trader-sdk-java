package com.xinchentechnote.exchange.szse.impl;

import com.finproto.codec.BinaryCodec;
import com.finproto.szse.bin.messages.*;
import com.xinchentechnote.exchange.common.ApiStatus;
import com.xinchentechnote.exchange.common.HandlerName;
import com.xinchentechnote.exchange.szse.SzseTraderSpi;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

public class SzseApiMessageHandler extends SimpleChannelInboundHandler<ByteBuf> {

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
        SzseBinary msg = new SzseBinary();
        msg.decode(byteBuf);
        System.out.println("Received message: " + msg);
        BinaryCodec body = msg.getBody();
        heartbeatTimeoutCounter = 0;
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
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state() == IdleState.READER_IDLE) {
                SzseBinary szseBinary = new SzseBinary();
                szseBinary.setMsgType(3);
                szseBinary.setBody(new Heartbeat());
                ByteBuf buffer = Unpooled.buffer();
                szseBinary.encode(buffer);
                ctx.writeAndFlush(buffer);
                System.out.println(szseBinary);
            }
        }
        super.userEventTriggered(ctx, evt);
    }
}
