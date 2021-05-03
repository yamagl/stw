package com.github.yamagl.stw.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class HttpClientHandler extends SimpleChannelInboundHandler<HttpObject> {

    private static final InternalLogger logger =
            InternalLoggerFactory.getInstance(HttpClientHandler.class);

    private final Channel inbound;

    public HttpClientHandler(Channel inbound) {
        this.inbound = inbound;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpResponse) {
            HttpResponse resp = (HttpResponse) msg;
            logger.info(resp.decoderResult().toString());
            inbound.writeAndFlush(resp);
        } else if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;
            inbound.writeAndFlush(Unpooled.copiedBuffer(content.content()));
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
