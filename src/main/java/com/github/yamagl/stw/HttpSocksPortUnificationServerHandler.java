package com.github.yamagl.stw;

import com.github.yamagl.stw.http.HttpHelloWorldServerHandler;
import com.github.yamagl.stw.socks5.SocksServerHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.codec.socksx.SocksVersion;
import io.netty.handler.codec.socksx.v4.Socks4ServerDecoder;
import io.netty.handler.codec.socksx.v4.Socks4ServerEncoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.List;

public class HttpSocksPortUnificationServerHandler extends ByteToMessageDecoder {

    private static final InternalLogger logger =
            InternalLoggerFactory.getInstance(HttpSocksPortUnificationServerHandler.class);

    private final Socks5ServerEncoder socks5encoder;

    public HttpSocksPortUnificationServerHandler() {
        this.socks5encoder = ObjectUtil.checkNotNull(Socks5ServerEncoder.DEFAULT, "socks5encoder");
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        final int readerIndex = in.readerIndex();
        if (in.writerIndex() == readerIndex) {
            return;
        }

        ChannelPipeline p = ctx.pipeline();
        final byte versionVal = in.getByte(readerIndex);
        SocksVersion version = SocksVersion.valueOf(versionVal);

        switch (version) {
            case SOCKS4a:
                logKnownVersion(ctx, version);
                p.addAfter(ctx.name(), null, Socks4ServerEncoder.INSTANCE);
                p.addAfter(ctx.name(), null, new Socks4ServerDecoder());
                removeHttpPipeline(p);
                break;
            case SOCKS5:
                logKnownVersion(ctx, version);
                p.addAfter(ctx.name(), null, socks5encoder);
                p.addAfter(ctx.name(), null, new Socks5InitialRequestDecoder());
                removeHttpPipeline(p);
                break;
            default:
                logUnknownVersion(ctx, versionVal);
                ctx.pipeline().remove(SocksServerHandler.class);
        }

        p.remove(this);
    }

    private void removeHttpPipeline(ChannelPipeline p) {
        p.remove(HttpServerCodec.class);
        p.remove(HttpServerExpectContinueHandler.class);
        p.remove(HttpHelloWorldServerHandler.class);
    }

    private static void logKnownVersion(ChannelHandlerContext ctx, SocksVersion version) {
        logger.debug("{} Protocol version: {}({})", ctx.channel(), version);
    }

    private static void logUnknownVersion(ChannelHandlerContext ctx, byte versionVal) {
        if (logger.isDebugEnabled()) {
            logger.debug("{} Unknown protocol version: {}", ctx.channel(), versionVal & 0xFF);
        }
    }
}
