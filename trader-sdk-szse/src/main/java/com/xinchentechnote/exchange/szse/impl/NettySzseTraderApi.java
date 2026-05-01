package com.xinchentechnote.exchange.szse.impl;

import com.finproto.codec.BinaryCodec;
import com.finproto.szse.bin.messages.*;
import com.xinchentechnote.exchange.common.ApiStatus;
import com.xinchentechnote.exchange.common.FrontInfoField;
import com.xinchentechnote.exchange.szse.SzseTraderApi;
import com.xinchentechnote.exchange.szse.SzseTraderSpi;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettySzseTraderApi implements SzseTraderApi {

    private static final Logger logger = LoggerFactory.getLogger(NettySzseTraderApi.class);

    private FrontInfoField frontInfoField;
    private ApiStatus status = ApiStatus.NEW;
    private SzseTraderSpi spi;
    private volatile Channel channel;

    @Override
    public ApiStatus getApiStatus() {
        return status;
    }

    @Override
    public String getApiVersion() {
        return "1.0.0";
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public ApiStatus getStatus() {
        return status;
    }

    public void setStatus(ApiStatus status) {
        this.status = status;
    }

    public SzseTraderSpi getSpi() {
        return spi;
    }

    public void setSpi(SzseTraderSpi spi) {
        this.spi = spi;
    }

    @Override
    public void init() {
        EventLoopGroup group = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class).handler(new SocketChannelChannelInitializer(this)).connect(this.frontInfoField.getIp(), this.frontInfoField.getPort()).addListener(future -> {
            if (future.isSuccess()) {
                logger.info("Successfully connected to front: {}", frontInfoField);
            } else {
                status = ApiStatus.DISCONNECTED;
                logger.error("Failed to connect to front: {}, reason: {}", frontInfoField, future.cause().getMessage());
            }
        });
    }

    @Override
    public void join() {

    }

    @Override
    public int getTradingDay() {
        return 0;
    }

    @Override
    public FrontInfoField getFrontInfo() {
        return frontInfoField;
    }

    @Override
    public void registerFront(String frontAddress) {
        this.frontInfoField = new FrontInfoField(frontAddress);
    }

    @Override
    public void registerSpi(SzseTraderSpi spi) {
        this.spi = spi;
    }

    private void sendMessage(int msgType, BinaryCodec body) {
        if (channel == null || !channel.isActive()) {
            throw new IllegalStateException("Channel not connected");
        }

        SzseBinary sseBinary = new SzseBinary();
        sseBinary.setMsgType(msgType);
        sseBinary.setBody(body);
        ByteBuf buffer = Unpooled.buffer();
        try {
            sseBinary.encode(buffer);
            channel.writeAndFlush(buffer);
            logger.debug("Sent message type: {}", msgType);
        } catch (Exception e) {
            logger.error("Failed to send message type: {}", msgType, e);
            throw new RuntimeException("Message send failed", e);
        }
    }

    @Override
    public void reqLogon(Logon logon) {
        sendMessage(1, logon);
    }

    @Override
    public void reqLogout(Logout logout) {
        sendMessage(2, logout);
    }

    @Override
    public void reqNewOrder(NewOrder newOrder) {
        sendMessage(100101, newOrder);
    }

    @Override
    public void reqOrderCancelRequest(OrderCancelRequest orderCancelRequest) {
        sendMessage(190007, orderCancelRequest);
    }

    private final SzseBinary heartbeat;

    public NettySzseTraderApi() {
         heartbeat = new SzseBinary();
         heartbeat.setMsgType(3);
         heartbeat.setBody(new Heartbeat());
    }

    public void sendHeartbeat() {
        ByteBuf buffer = Unpooled.buffer();
        heartbeat.encode(buffer);
        channel.writeAndFlush(buffer);
    }
}
