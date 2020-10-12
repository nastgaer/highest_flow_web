package com.useful.server.config;

import com.useful.server.channelInitializer.ServerChannelInitializer;
import com.useful.server.listener.ServerListener;
import com.useful.server.listener.impl.ServerListenerImpl;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class Beans {

    @Autowired
    @Qualifier("serverChannelInitializer")
    private ServerChannelInitializer serverChannelInitializer;

    @Value("${tcp.host}")
    private String host;

    @Value("${tcp.port}")
    private int port;

    @Value("${boss.thread.count}")
    private int bossCount;

    @Value("${worker.thread.count}")
    private int workerCount;

    @Value("${so.keepAlive}")
    private boolean keepAlive;

    @Value("${so.backlog}")
    private int backlog;

    @Bean(name = "serverBootstrap")
    public ServerBootstrap serverBootstrap() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup(), workerGroup());
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.handler(new LoggingHandler(LogLevel.DEBUG));
        serverBootstrap.childHandler(serverChannelInitializer);

        Map<ChannelOption<?>, Object> options = tcpChannelOptions();
        for (ChannelOption option : options.keySet()) {
            serverBootstrap.option(option, options.get(option));
        }

        return serverBootstrap;
    }

    @Bean(name = "bossGroup", destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup bossGroup() {
        return new NioEventLoopGroup(bossCount);
    }

    @Bean(name = "workerGroup", destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup workerGroup() {
        return new NioEventLoopGroup(workerCount);
    }

    @Bean(name = "tcpChannelOptions")
    public Map<ChannelOption<?>, Object> tcpChannelOptions() {
        Map<ChannelOption<?>, Object> options = new HashMap<ChannelOption<?>, Object>();

        options.put(ChannelOption.SO_KEEPALIVE, keepAlive);
        options.put(ChannelOption.SO_BACKLOG, backlog);
        options.put(ChannelOption.TCP_NODELAY, true);

        return options;
    }

    @Bean(name = "tcpSocketAddress")
    public InetSocketAddress tcpSocketAddress() {
        return new InetSocketAddress(host, port);
    }

    @Bean(name = "serverListener")
    public ServerListener serverListener() {
        return new ServerListenerImpl();
    }
}
