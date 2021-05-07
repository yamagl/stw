package com.github.yamagl.stw;

import com.github.yamagl.stw.http.HttpServerHandler;
import com.github.yamagl.stw.socks5.SocksServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class HttpSocksServerInitializer extends ChannelInitializer<SocketChannel> {

    private final String host;
    private final int port;

    public HttpSocksServerInitializer(String host, int port) {
        this.host = host;
        this.port = port;
    }


    @Override
    public void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
        ch.pipeline().addLast(new HttpSocksPortUnificationServerHandler());
        ch.pipeline().addLast(SocksServerHandler.INSTANCE);
        ch.pipeline().addLast(new HttpServerCodec());
        ch.pipeline().addLast(new HttpServerExpectContinueHandler());
        ch.pipeline().addLast(new HttpObjectAggregator(1048576));
        ch.pipeline().addLast(new HttpServerHandler(host, port));
    }
}
