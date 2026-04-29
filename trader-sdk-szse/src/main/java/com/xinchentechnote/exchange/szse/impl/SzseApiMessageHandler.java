package com.xinchentechnote.exchange.szse.impl;

import com.finproto.szse.bin.messages.Heartbeat;
import com.finproto.szse.bin.messages.SzseBinary;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class SzseApiMessageHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
            SzseBinary msg = new SzseBinary();
            msg.decode(byteBuf);
            System.out.println("Received message: " + msg);
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
