package com.github.yamagl.stw.http;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static io.netty.handler.codec.http.HttpHeaderNames.HOST;

public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {
    private final Bootstrap b = new Bootstrap();
    private final String host;
    private final int port;
    private static final InternalLogger logger =
            InternalLoggerFactory.getInstance(HttpServerHandler.class);

    public HttpServerHandler(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof FullHttpRequest) {
            final FullHttpRequest inboundReq = (FullHttpRequest) msg;

            String content = inboundReq.content().toString(Charset.defaultCharset());

            logger.info(content);

            b.group(ctx.channel().eventLoop())
                    .channel(ctx.channel().getClass())
                    .handler(new ChannelInitializer<SocketChannel> () {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                            ch.pipeline().addLast(new HttpClientCodec());
                            ch.pipeline().addLast(new HttpClientHandler(ctx.channel()));
                        }
                    });

            b.connect(host, port).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    FullHttpRequest outboundReq = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, inboundReq.method(), inboundReq.uri());
                    outboundReq.headers().setAll(inboundReq.headers());
                    outboundReq.headers().set(HOST, host);
                    outboundReq.content().writeBytes(content.getBytes(StandardCharsets.UTF_8));

                    future.channel().writeAndFlush(outboundReq);
                    future.channel().closeFuture().sync();
                } else {
                    ctx.close();
                }
            });
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
