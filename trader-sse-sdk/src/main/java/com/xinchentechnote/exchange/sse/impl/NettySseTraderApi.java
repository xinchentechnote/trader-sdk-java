package com.xinchentechnote.exchange.sse.impl;

import com.finproto.codec.BinaryCodec;
import com.finproto.sse.bin.messages.Heartbeat;
import com.finproto.sse.bin.messages.Logon;
import com.finproto.sse.bin.messages.Logout;
import com.finproto.sse.bin.messages.SseBinary;
import com.xinchentechnote.exchange.sse.SseTraderApi;
import com.xinchentechnote.exchange.sse.SseTraderSpi;
import com.xinchentechnote.exchange.sse.dto.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;

@Data
public class NettySseTraderApi implements SseTraderApi {

    static final Heartbeat heartbeat = new Heartbeat();

    private FrontInfoField frontInfo;
    private SseTraderSpi spi;
    private volatile Channel channel;

    private ApiStatus status;

    private static NioEventLoopGroup workerGroup = new NioEventLoopGroup(1);

    public NettySseTraderApi() {
        status = ApiStatus.NEW;
    }

    @Override
    public String GetApiVersion() {
        return "";
    }

    @Override
    public void Init() {
        status = ApiStatus.CONNECTING;

        Bootstrap bootstrap = new Bootstrap();
        ChannelFuture future = bootstrap
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new SocketChannelChannelInitializer(this))
                .connect(this.frontInfo.getIp(), this.frontInfo.getPort())
                .addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                System.out.println("连接成功");
            } else {
                System.out.println("连接失败");
                status = ApiStatus.ERROR;
            }
        });
        try {
            future.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void Join() {

    }

    @Override
    public int GetTradingDay() {
        return 0;
    }

    @Override
    public FrontInfoField GetFrontInfo() {
        return null;
    }

    @Override
    public void RegisterFront(String frontAddress) {
        //tcp://182.254.243.31:40001
        this.frontInfo = new FrontInfoField(frontAddress);
    }

    @Override
    public void RegisterNameServer(String nameServerAddress) {

    }

    @Override
    public void RegisterSpi(SseTraderSpi spi) {
        this.spi = spi;
    }

    @Override
    public void ReqUserLogin(ReqUserLoginField reqUserLoginField, int requestId) {
        if (status != ApiStatus.CONNECTED) {
            System.out.println("Cannot login: not connected");
            return;
        }
        Logon logon = new Logon();
        logon.setSenderCompId("send");
        logon.setTargetCompId("target");
        logon.setQsize(10);
        logon.setHeartBtInt((short) 5);
        logon.setPrtclVersion("1.0");
        logon.setTradeDate(20260420);
        send(logon);
        // Status will be updated when response is received
    }

    public void sendHeartbeat() {
        sendMessage(33, heartbeat);
    }

    private void send(Logon logon) {
        sendMessage(40, logon);
    }

    private void sendMessage(int msgType, BinaryCodec body) {
        SseBinary sseBinary = new SseBinary();
        sseBinary.setMsgType(msgType);
        sseBinary.setBody(body);
        ByteBuf buffer = Unpooled.buffer();
        sseBinary.encode(buffer);
        this.channel.writeAndFlush(buffer);
    }

    @Override
    public void ReqUserLogout(ReqUserLogoutField reqUserLoginField, int requestId) {
        if (status != ApiStatus.LOGGED_IN) {
            System.out.println("Cannot logout: not logged in");
            return;
        }
        Logout logout = new Logout();
        logout.setSessionStatus(1);
        logout.setText("logout");
        sendMessage(41, logout);
        status = ApiStatus.LOGGING_OUT;
    }

    @Override
    public int ReqOrderInsert(InputOrderField inputOrderField, int requestId) {
        return 0;
    }

    @Override
    public int ReqOrderAction(InputOrderActionField inputOrderActionField, int requestId) {
        return 0;
    }

}
