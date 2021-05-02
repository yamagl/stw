package com.github.yamagl;

import com.github.yamagl.http.HttpHelloWorldServerHandler;
import com.github.yamagl.socksproxy.SocksServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class HttpSocksServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
        ch.pipeline().addLast(new HttpSocksPortUnificationServerHandler());
        ch.pipeline().addLast(SocksServerHandler.INSTANCE);
        ch.pipeline().addLast(new HttpServerCodec());
        ch.pipeline().addLast(new HttpServerExpectContinueHandler());
        ch.pipeline().addLast(new HttpHelloWorldServerHandler());
    }
}
