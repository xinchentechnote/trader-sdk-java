package com.xinchentechnote.exchange.szse.impl;

import com.finproto.szse.bin.messages.Logon;
import com.finproto.szse.bin.messages.Logout;
import com.xinchentechnote.exchange.szse.SzseTraderApi;
import com.xinchentechnote.exchange.szse.SzseTraderSpi;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettySzseTraderApi implements SzseTraderApi {

    private static final Logger logger = LoggerFactory.getLogger(NettySzseTraderApi.class);

    private FrontInfoField frontInfoField;
    private ApiStatus status = ApiStatus.NEW;
    private SzseTraderSpi szseTraderSpi;

    @Override
    public ApiStatus getApiStatus() {
        return status;
    }

    @Override
    public String getApiVersion() {
        return "1.0.0";
    }

    @Override
    public void init() {
        EventLoopGroup group = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        if (NettyLoggingUtil.isLoggingEnabled()) {
                            pipeline.addLast(NettyLoggingUtil.getLoggingHandlerName(), new LoggingHandler(NettyLoggingUtil.getLoggingLevel()));
                        }
                        pipeline.addLast(HandlerName.FRAME, new LengthFieldBasedFrameDecoder(1024 * 1024, // 最大帧长度
                                        12,           // 长度字段偏移量（MsgType占4字节）
                                        4,           // 长度字段长度（MsgSeqNum占8字节）
                                        4,           // 长度调整 + 4byte checksum
                                        0           // 初始字节剥离
                                )).addLast(HandlerName.IDLE, new IdleStateHandler(HeartBtIntUtil.MIN, 0, 0))
                                .addLast(HandlerName.MESSAGE, new SzseApiMessageHandler());
                    }
                })
                .connect(this.frontInfoField.getIp(), this.frontInfoField.getPort()).addListener(future -> {
                    if (future.isSuccess()) {
                        status = ApiStatus.CONNECTED;
                        szseTraderSpi.onFrontConnected();
                        logger.info("Successfully connected to front: {}", frontInfoField);
                    } else {
                        status = ApiStatus.DISCONNECTED;
                        szseTraderSpi.onFrontDisconnected(0);
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
        this.szseTraderSpi = spi;
    }

    @Override
    public void reqLogon(Logon logon) {

    }

    @Override
    public void reqLogout(Logout logout) {

    }
}
