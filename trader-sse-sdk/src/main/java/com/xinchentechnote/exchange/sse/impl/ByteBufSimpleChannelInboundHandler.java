package com.xinchentechnote.exchange.sse.impl;

import com.finproto.sse.bin.messages.Logon;
import com.finproto.sse.bin.messages.Logout;
import com.finproto.sse.bin.messages.SseBinary;
import com.xinchentechnote.exchange.sse.dto.RspInfoField;
import com.xinchentechnote.exchange.sse.dto.RspUserLoginField;
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
            nettySseTraderApi.getSpi().OnFrontConnected();
        }
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Disconnected from " + ctx.channel().remoteAddress());
        nettySseTraderApi.setStatus(ApiStatus.DISCONNECTED);
        if (nettySseTraderApi.getSpi() != null) {
            nettySseTraderApi.getSpi().OnFrontDisconnected(0); // 假设原因0
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
                    RspUserLoginField rsp = new RspUserLoginField();
                    // 填充rsp，如果需要
                    RspInfoField rspInfo = new RspInfoField();
                    Logon logon = (Logon) msg.getBody();
                    short heartBtInt = logon.getHeartBtInt();
                    ChannelPipeline pipeline = channelHandlerContext.pipeline();
                    pipeline.remove("idle");
                    pipeline.addAfter("frame", "idle", new IdleStateHandler(heartBtInt, 0, 0));
                    nettySseTraderApi.getSpi().OnLogon(logon);
                }
                break;
            case 41: // Logout response
                nettySseTraderApi.setStatus(ApiStatus.DISCONNECTED); // 或 LOGOUT
                if (nettySseTraderApi.getSpi() != null) {
                    RspInfoField rspInfo = new RspInfoField();
                    nettySseTraderApi.getSpi().OnLogout((Logout) msg.getBody());
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
                nettySseTraderApi.sendHeartbeat();
            }
        }
        super.userEventTriggered(ctx, evt);
    }
}
