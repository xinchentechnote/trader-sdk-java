package com.xinchentechnote.exchange.sse.impl;

import com.xinchentechnote.exchange.sse.SseTraderApi;
import com.xinchentechnote.exchange.sse.SseTraderSpi;
import com.xinchentechnote.exchange.sse.dto.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class NettySseTraderApi implements SseTraderApi {

    private String flowPath;
    private String ip;
    private int port;
    private SseTraderSpi spi;

    private static NioEventLoopGroup workerGroup = new NioEventLoopGroup(1);

    public NettySseTraderApi(String flowPath) {
        this.flowPath = flowPath;
    }

    @Override
    public String GetApiVersion() {
        return "";
    }

    @Override
    public void Init() {

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(
                                1024 * 1024, // 最大帧长度
                                12,           // 长度字段偏移量（MsgType占4字节）
                                4,           // 长度字段长度（MsgSeqNum占8字节）
                                4,           // 长度调整 + 4byte checksum
                                0           // 初始字节剥离
                        )).addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                System.out.println("Connected to " + ctx.channel().remoteAddress());
                                super.channelActive(ctx);
                            }

                            @Override
                            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                System.out.println("Disconnected from " + ctx.channel().remoteAddress());
                                super.channelInactive(ctx);
                            }

                            @Override
                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
                                System.out.println("Received msg: " + byteBuf.readableBytes());
                            }
                        });
                    }
                })
                .connect(ip,port).addListener(
                        (ChannelFutureListener) channelFuture -> {
                            if (channelFuture.isSuccess()) {
                                System.out.println("连接成功");
                            } else {
                                System.out.println("连接失败");
                            }
                });


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
        String[] split = frontAddress.split("//");
        String[] addr = split[1].split(":");
        this.ip = addr[0];
        this.port = Integer.parseInt(addr[1]);
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

    }

    @Override
    public void ReqUserLogout(ReqUserLogoutField reqUserLoginField, int requestId) {

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
