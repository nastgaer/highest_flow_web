package com.useful.server.typeServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

@Component
public class TCPServer {

    @Autowired
    @Qualifier("serverBootstrap")
    private ServerBootstrap serverBootstrap;

    @Autowired
    @Qualifier("tcpSocketAddress")
    private InetSocketAddress tcpSocketAddress;

    @Autowired
    @Qualifier("bossGroup")
    private NioEventLoopGroup bossGroup;

    @Autowired
    @Qualifier("workerGroup")
    private NioEventLoopGroup workerGroup;

    private boolean restartFlag = false;

    private int restartTimeout = 3;

    public void start() throws Exception {
        ChannelFuture channelFuture = serverBootstrap.bind(tcpSocketAddress).sync();
        channelFuture.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    System.out.println("启动成功");
                } else {
                    if (restartFlag) {
                        restart();
                    }
                }
            }
        });
        channelFuture.channel().closeFuture().sync();
    }

    public void stop() throws Exception {
        restartFlag = false;
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public void restart() {
        try {
            Thread.sleep(restartTimeout * 1000);
            start();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
