package com.github.yamagl.stw.socks5;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import io.netty.handler.codec.socksx.v5.Socks5Message;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;


public class SocksServerConnectHandler extends SimpleChannelInboundHandler<Socks5Message> {
    private final Bootstrap b = new Bootstrap();
    private static final InternalLogger logger =
            InternalLoggerFactory.getInstance(SocksServerConnectHandler.class);

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, final Socks5Message message) {
        if (message instanceof Socks5CommandRequest) {
            final Socks5CommandRequest request = (Socks5CommandRequest) message;
            logger.info(String.format("%s %s %s", request.dstAddr(), request.dstPort(), request.dstAddrType()));
            Promise<Channel> promise = ctx.executor().newPromise();
            promise.addListener(
                    (FutureListener<Channel>) future -> {
                        final Channel outboundChannel = future.getNow();
                        if (future.isSuccess()) {
                            ChannelFuture responseFuture =
                                    ctx.channel().writeAndFlush(new DefaultSocks5CommandResponse(
                                            Socks5CommandStatus.SUCCESS,
                                            request.dstAddrType(),
                                            request.dstAddr(),
                                            request.dstPort()));

                            responseFuture.addListener((ChannelFutureListener) channelFuture -> {
                                ctx.pipeline().remove(SocksServerConnectHandler.this);
                                outboundChannel.pipeline().addLast(new RelayHandler(ctx.channel()));
                                ctx.pipeline().addLast(new RelayHandler(outboundChannel));
                            });
                        } else {
                            ctx.channel().writeAndFlush(new DefaultSocks5CommandResponse(
                                    Socks5CommandStatus.FAILURE, request.dstAddrType()));
                            SocksServerUtils.closeOnFlush(ctx.channel());
                        }
                    });

            final Channel inboundChannel = ctx.channel();
            b.group(inboundChannel.eventLoop())
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new DirectClientHandler(promise));

            b.connect(request.dstAddr(), request.dstPort()).addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    ctx.channel().writeAndFlush(
                            new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, request.dstAddrType()));
                    SocksServerUtils.closeOnFlush(ctx.channel());
                }
            });
        } else {
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        SocksServerUtils.closeOnFlush(ctx.channel());
    }
}
