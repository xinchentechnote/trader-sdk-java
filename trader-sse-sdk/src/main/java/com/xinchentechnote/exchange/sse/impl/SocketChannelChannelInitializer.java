package com.xinchentechnote.exchange.sse.impl;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

class SocketChannelChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final NettySseTraderApi nettySseTraderApi;

    public SocketChannelChannelInitializer(NettySseTraderApi nettySseTraderApi) {
        this.nettySseTraderApi = nettySseTraderApi;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(HandlerName.FRAME, new LengthFieldBasedFrameDecoder(1024 * 1024, // 最大帧长度
                        12,           // 长度字段偏移量（MsgType占4字节）
                        4,           // 长度字段长度（MsgSeqNum占8字节）
                        4,           // 长度调整 + 4byte checksum
                        0           // 初始字节剥离
                )).addLast(HandlerName.IDLE, new IdleStateHandler(3, 0, 0))
                .addLast(new ByteBufSimpleChannelInboundHandler(nettySseTraderApi));
    }
}
