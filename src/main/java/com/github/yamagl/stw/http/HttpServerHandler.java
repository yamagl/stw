package com.github.yamagl.stw.http;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import static io.netty.handler.codec.http.HttpHeaderNames.*;

public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {
    private final Bootstrap b = new Bootstrap();
    private final String host;
    private final int port;

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
        if (msg instanceof HttpRequest) {
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
                    HttpRequest outboundReq = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
                    outboundReq.headers().set(HOST, "www.baidu.com");
                    outboundReq.headers().set(ACCEPT, "*/*");
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
