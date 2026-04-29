package com.xinchentechnote.exchange.sse.impl;

import com.finproto.codec.BinaryCodec;
import com.finproto.sse.bin.messages.*;
import com.xinchentechnote.exchange.sse.SseTraderApi;
import com.xinchentechnote.exchange.sse.SseTraderSpi;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Data
public class NettySseTraderApi implements SseTraderApi {

    private static final Logger logger = LoggerFactory.getLogger(NettySseTraderApi.class);

    static final Heartbeat heartbeat = new Heartbeat();

    private FrontInfoField frontInfo;
    private SseTraderSpi spi;
    private volatile Channel channel;

    private final AtomicReference<ApiStatus> status = new AtomicReference<>(ApiStatus.NEW);

    private final NioEventLoopGroup workerGroup = new NioEventLoopGroup(1);

    public NettySseTraderApi() {
        // status initialized in field
    }

    @Override
    public String getApiVersion() {
        return "1.0.0";
    }

    @Override
    public void init() {
        if (frontInfo == null) {
            throw new IllegalStateException("Front not registered. Call registerFront() first.");
        }

        if (!status.compareAndSet(ApiStatus.NEW, ApiStatus.CONNECTING)) {
            logger.warn("Init called in invalid state: {}", status.get());
            return;
        }

        Bootstrap bootstrap = new Bootstrap();
        ChannelFuture future = bootstrap
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new SocketChannelChannelInitializer(this))
                .connect(this.frontInfo.getIp(), this.frontInfo.getPort())
                .addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                logger.info("Connection successful to {}:{}", frontInfo.getIp(), frontInfo.getPort());
                spi.onFrontConnected();
            } else {
                logger.error("Connection failed to {}:{}", frontInfo.getIp(), frontInfo.getPort(), channelFuture.cause());
                status.set(ApiStatus.ERROR);
                if (spi != null) {
                    spi.onFrontDisconnected(-1); // Error code for connection failure
                }
            }
        });
        try {
            future.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during connection", e);
        }
    }

    @Override
    public void join() {
        try {
            workerGroup.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Join interrupted", e);
        }
    }

    @Override
    public ApiStatus getApiStatus() {
        return status.get();
    }

    @Override
    public int getTradingDay() {
        // Return current date in YYYYMMDD format, e.g., 20260421
        return 20260421; // Placeholder, implement actual logic
    }

    @Override
    public FrontInfoField getFrontInfo() {
        return this.frontInfo;
    }

    @Override
    public void registerFront(String frontAddress) {
        if (frontAddress == null || !frontAddress.startsWith("tcp://")) {
            throw new IllegalArgumentException("Invalid front address: " + frontAddress);
        }
        this.frontInfo = new FrontInfoField(frontAddress);
        logger.info("Registered front: {}", frontAddress);
    }

    @Override
    public void registerSpi(SseTraderSpi spi) {
        this.spi = spi;
        logger.info("Registered SPI: {}", spi.getClass().getSimpleName());
    }

    @Override
    public void reqLogon(Logon logon) {
        if (status.get() != ApiStatus.CONNECTED) {
            logger.warn("Cannot login: not connected, current status: {}", status.get());
            return;
        }
        sendMessage(SseBinary.BodyMessageFactory.MessageType.LOGON.getValue(), logon);
    }

    public void sendHeartbeat() {
        sendMessage(SseBinary.BodyMessageFactory.MessageType.HEARTBEAT.getValue(), heartbeat);
    }

    private void sendMessage(int msgType, BinaryCodec body) {
        if (channel == null || !channel.isActive()) {
            throw new IllegalStateException("Channel not connected");
        }

        SseBinary sseBinary = new SseBinary();
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
    public void reqLogout(Logout logout) {
        if (status.get() != ApiStatus.LOGGED_IN) {
            logger.warn("Cannot logout: not logged in, current status: {}", status.get());
            return;
        }
        status.set(ApiStatus.LOGGING_OUT);
        sendMessage(SseBinary.BodyMessageFactory.MessageType.LOGOUT.getValue(), logout);
    }

    @Override
    public int reqNewOrderSingle(NewOrderSingle newOrderSingle) {
        if (status.get() != ApiStatus.LOGGED_IN) {
            logger.warn("Cannot send order: not logged in, current status: {}", status.get());
            return -1;
        }

        sendMessage(SseBinary.BodyMessageFactory.MessageType.NEW_ORDER_SINGLE.getValue(), newOrderSingle);
        return 0;
    }

    @Override
    public int reqOrderCancel(OrderCancel orderCancel) {
        if (status.get() != ApiStatus.LOGGED_IN) {
            logger.warn("Cannot cancel order: not logged in, current status: {}", status.get());
            return -1;
        }

        sendMessage(SseBinary.BodyMessageFactory.MessageType.ORDER_CANCEL.getValue(), orderCancel);
        return 0;
    }

    @Override
    public int reqExecRptSync(ExecRptSync execRptSync) {
        if (status.get() != ApiStatus.LOGGED_IN) {
            logger.warn("Cannot cancel order: not logged in, current status: {}", status.get());
            return -1;
        }
        sendMessage(SseBinary.BodyMessageFactory.MessageType.EXEC_RPT_SYNC.getValue(), execRptSync);
        return 0;
    }

    public void shutdown() {
        logger.info("Shutting down NettySseTraderApi");
        if (channel != null && channel.isActive()) {
            channel.close();
        }
        workerGroup.shutdownGracefully();
    }
}
