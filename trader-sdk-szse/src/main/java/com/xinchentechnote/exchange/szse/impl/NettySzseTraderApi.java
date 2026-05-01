package com.xinchentechnote.exchange.szse.impl;

import com.finproto.szse.bin.messages.Logon;
import com.finproto.szse.bin.messages.Logout;
import com.xinchentechnote.exchange.common.ApiStatus;
import com.xinchentechnote.exchange.common.FrontInfoField;
import com.xinchentechnote.exchange.szse.SzseTraderApi;
import com.xinchentechnote.exchange.szse.SzseTraderSpi;
import io.netty.bootstrap.Bootstrap;
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
    private Channel channel;

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
                status = ApiStatus.CONNECTED;
                spi.onFrontConnected();
                logger.info("Successfully connected to front: {}", frontInfoField);
            } else {
                status = ApiStatus.DISCONNECTED;
                spi.onFrontDisconnected(0);
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

    @Override
    public void reqLogon(Logon logon) {

    }

    @Override
    public void reqLogout(Logout logout) {

    }

}
