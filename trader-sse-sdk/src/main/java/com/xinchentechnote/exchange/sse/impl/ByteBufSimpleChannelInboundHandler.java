package com.xinchentechnote.exchange.sse.impl;

import com.finproto.sse.bin.messages.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

class ByteBufSimpleChannelInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final NettySseTraderApi nettySseTraderApi;

    public ByteBufSimpleChannelInboundHandler(NettySseTraderApi nettySseTraderApi) {
        this.nettySseTraderApi = nettySseTraderApi;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Connected to " + ctx.channel().remoteAddress());
        nettySseTraderApi.setChannel(ctx.channel());
        nettySseTraderApi.setStatus(ApiStatus.CONNECTED);
        if (nettySseTraderApi.getSpi() != null) {
            nettySseTraderApi.getSpi().onFrontConnected();
        }
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Disconnected from " + ctx.channel().remoteAddress());
        nettySseTraderApi.setStatus(ApiStatus.DISCONNECTED);
        if (nettySseTraderApi.getSpi() != null) {
            nettySseTraderApi.getSpi().onFrontDisconnected(0); // 假设原因0
        }
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
        SseBinary msg = new SseBinary();
        msg.decode(byteBuf);
        System.out.println("Received msg: " + msg);
        int msgType = msg.getMsgType();
        switch (msgType) {
            case 33: // Heartbeat
                // 处理心跳
                break;
            case 40: // Logon response
                nettySseTraderApi.setStatus(ApiStatus.LOGGED_IN);
                if (nettySseTraderApi.getSpi() != null) {
                    Logon logon = (Logon) msg.getBody();
                    short heartBtInt = logon.getHeartBtInt();
                    ChannelPipeline pipeline = channelHandlerContext.pipeline();
                    pipeline.remove("idle");
                    pipeline.addAfter("frame", "idle", new IdleStateHandler(heartBtInt, 0, 0));
                    nettySseTraderApi.getSpi().onLogon(logon);
                }
                break;
            case 41: // Logout response
                nettySseTraderApi.setStatus(ApiStatus.DISCONNECTED); // 或 LOGOUT
                if (nettySseTraderApi.getSpi() != null) {
                    nettySseTraderApi.getSpi().onLogout((Logout) msg.getBody());
                }
                break;
            case 32:
                if (nettySseTraderApi.getSpi() != null) {
                    nettySseTraderApi.getSpi().onConfirm((Confirm) msg.getBody());
                }
                break;
            case 59:
                if (nettySseTraderApi.getSpi() != null) {
                    nettySseTraderApi.getSpi().onCancelReject((CancelReject) msg.getBody());
                }
                break;
            case 103:
                if (nettySseTraderApi.getSpi() != null) {
                    nettySseTraderApi.getSpi().onReport((Report) msg.getBody());
                }
                break;
            default:
                System.out.println("Unknown message type: " + msgType);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state() == IdleState.READER_IDLE) {
                if (nettySseTraderApi.getSpi() != null) {
                    nettySseTraderApi.getSpi().onHeartBeatWarning(0);
                }
                nettySseTraderApi.sendHeartbeat();
            }
        }
        super.userEventTriggered(ctx, evt);
    }
}
