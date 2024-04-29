package com.nettychat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NettyChatApplication {

    public static void main(String[] args) throws Exception{
        SpringApplication.run(NettyChatApplication.class, args);
        EventLoopGroup bossGroup = new NioEventLoopGroup(1); // boss group을 설정합니다.
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // worker group을 설정합니다.
        try {
            ServerBootstrap b = new ServerBootstrap(); // ServerBootstrap 객체를 생성합니다.
            b.group(bossGroup, workerGroup) // bossGroup과 workerGroup을 할당합니다.
                    .channel(NioServerSocketChannel.class) // 채널 유형을 NIO로 설정합니다.
                    .childHandler(new ChannelInitializer<SocketChannel>() { // 채널 초기화 핸들러를 추가합니다.
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new HttpServerCodec()); // HTTP 메시지를 처리하는 코덱을 파이프라인에 추가합니다.
                            ch.pipeline().addLast(new HttpObjectAggregator(65536)); // HTTP 메시지 조각을 하나의 메시지로 결합합니다.
                            ch.pipeline().addLast(new WebSocketServerProtocolHandler("/chat")); // WebSocket 경로를 설정합니다.
                            ch.pipeline().addLast(new WebSocketFrameHandler()); // WebSocket 프레임을 처리하는 핸들러를 추가합니다.
                        }
                    });

            ChannelFuture f = b.bind(8000).sync(); // 서버를 8080 포트에 바인드하고 시작합니다.
            f.channel().closeFuture().sync(); // 서버 채널이 닫힐 때까지 대기합니다.
        } finally {
            bossGroup.shutdownGracefully(); // bossGroup을 안전하게 종료합니다.
            workerGroup.shutdownGracefully(); // workerGroup을 안전하게 종료합니다.
        }
    }
}


