package com.xinchentechnote.exchange.sse.impl;

import com.finproto.sse.bin.messages.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ByteBufSimpleChannelInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final Logger logger = LoggerFactory.getLogger(ByteBufSimpleChannelInboundHandler.class);

    private final NettySseTraderApi nettySseTraderApi;

    private int heartbeatTimeoutCounter = 0;

    public ByteBufSimpleChannelInboundHandler(NettySseTraderApi nettySseTraderApi) {
        this.nettySseTraderApi = nettySseTraderApi;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Connected to {}", ctx.channel().remoteAddress());
        nettySseTraderApi.setChannel(ctx.channel());
        nettySseTraderApi.getStatus().set(ApiStatus.CONNECTED);
        if (nettySseTraderApi.getSpi() != null) {
            nettySseTraderApi.getSpi().onFrontConnected();
        }
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Disconnected from {}", ctx.channel().remoteAddress());
        nettySseTraderApi.getStatus().set(ApiStatus.DISCONNECTED);
        if (nettySseTraderApi.getSpi() != null) {
            nettySseTraderApi.getSpi().onFrontDisconnected(0); // Assuming reason 0
        }
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
        SseBinary msg = new SseBinary();
        msg.decode(byteBuf);
        logger.debug("Received message: {}", msg);
        int msgType = msg.getMsgType();
        SseBinary.BodyMessageFactory.MessageType messageType = SseBinary.BodyMessageFactory.MessageType.fromValue(msgType);
        switch (messageType) {
            case HEARTBEAT: // Heartbeat
                heartbeatTimeoutCounter = 0; // Reset timeout counter on any message received
                break;
            case LOGON: // Logon response
                nettySseTraderApi.getStatus().set(ApiStatus.LOGGED_IN);
                if (nettySseTraderApi.getSpi() != null) {
                    Logon logon = (Logon) msg.getBody();
                    int heartBtInt = HeartBtIntUtil.calculate(logon.getHeartBtInt());
                    if (!HeartBtIntUtil.isMin(heartBtInt)) {
                        ChannelPipeline pipeline = channelHandlerContext.pipeline();
                        pipeline.remove(HandlerName.IDLE);
                        pipeline.addAfter(HandlerName.FRAME, HandlerName.IDLE, new IdleStateHandler(heartBtInt, 0, 0));

                    }
                 nettySseTraderApi.getSpi().onLogon(logon);
                }
                break;
            case LOGOUT: // Logout response
                nettySseTraderApi.getStatus().set(ApiStatus.DISCONNECTED);
                if (nettySseTraderApi.getSpi() != null) {
                    nettySseTraderApi.getSpi().onLogout((Logout) msg.getBody());
                }
                break;
            case CONFIRM:
                if (nettySseTraderApi.getSpi() != null) {
                    nettySseTraderApi.getSpi().onConfirm((Confirm) msg.getBody());
                }
                break;
            case CANCEL_REJECT:
                if (nettySseTraderApi.getSpi() != null) {
                    nettySseTraderApi.getSpi().onCancelReject((CancelReject) msg.getBody());
                }
                break;
            case REPORT:
                if (nettySseTraderApi.getSpi() != null) {
                    nettySseTraderApi.getSpi().onReport((Report) msg.getBody());
                }
                break;
            default:
                logger.warn("Unknown message type: {}", msgType);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state() == IdleState.READER_IDLE) {
                if (nettySseTraderApi.getSpi() != null && heartbeatTimeoutCounter > 0) {
                    nettySseTraderApi.getSpi().onHeartBeatWarning(heartbeatTimeoutCounter);
                }
                heartbeatTimeoutCounter ++;
                nettySseTraderApi.sendHeartbeat();
                if (heartbeatTimeoutCounter >= 3) {
                    logger.warn("Heartbeat timeout, closing connection");
                    ctx.close();
                }
            }
        }
        super.userEventTriggered(ctx, evt);
    }
}
