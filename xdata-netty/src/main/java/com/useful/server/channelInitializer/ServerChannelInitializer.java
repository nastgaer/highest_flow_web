package com.useful.server.channelInitializer;

import com.useful.common.protobuf.Message;
import com.useful.server.handler.LogicHandler;
import com.useful.server.handler.ServerAuthHandler;
import com.useful.server.listener.ServerListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Value("${socket.READER_IDLE_TIME_SECONDS}")
    private int READER_IDLE_TIME_SECONDS;

    @Value("${socket.WRITER_IDLE_TIME_SECONDS}")
    private int WRITER_IDLE_TIME_SECONDS;

    @Value("${socket.ALL_IDLE_TIME_SECONDS}")
    private int ALL_IDLE_TIME_SECONDS;

    @Autowired
    private ServerListener serverListener;

    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast("idleStateHandler", new IdleStateHandler(READER_IDLE_TIME_SECONDS,
                WRITER_IDLE_TIME_SECONDS, ALL_IDLE_TIME_SECONDS, TimeUnit.SECONDS));
        pipeline.addLast(new ProtobufVarint32FrameDecoder());//添加protobuff解码器
        pipeline.addLast(new ProtobufDecoder(Message.MessageBase.getDefaultInstance()));//添加protobuff对应类解码器
        pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());//protobuf的编码器 和上面对对应
        pipeline.addLast(new ProtobufEncoder());//protobuf的编码器

//        pipeline.addLast("serverAuthHandler", serverAuthHandler);
//        pipeline.addLast("logicHandler", logicHandler);

        pipeline.addLast("serverAuthHandler", new ServerAuthHandler(serverListener));
        pipeline.addLast("logicHandler", new LogicHandler(serverListener));
    }
}
