package com.nettychat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    // 모든 클라이언트 채널을 저장하는 ChannelGroup 생성
    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // 새 클라이언트 연결이 생길 때 ChannelGroup에 추가
        channels.add(ctx.channel());
        log.info("ctx.channerl = {}", ctx.channel());
        log.info(channels.toString());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // 클라이언트 연결이 끊길 때 ChannelGroup에서 제거
        channels.remove(ctx.channel());
        log.info("ctx.channerl = {}", ctx.channel());
        log.info(channels.toString());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof TextWebSocketFrame) {
            // 클라이언트로부터 받은 메시지를 모든 클라이언트에게 브로드캐스트
            String message = ((TextWebSocketFrame) frame).text();
            for (Channel c : channels) {
                c.writeAndFlush(new TextWebSocketFrame(message));
                log.info("message={}", message);
            }
        }
    }
}
