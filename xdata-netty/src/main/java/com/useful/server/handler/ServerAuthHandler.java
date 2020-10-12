package com.useful.server.handler;

import com.useful.common.protobuf.Command;
import com.useful.common.protobuf.Message;
import com.useful.server.listener.ServerListener;
import com.useful.server.session.SessionManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;

public class ServerAuthHandler extends ChannelInboundHandlerAdapter {

    private static final int MAX_UN_REC_PING_TIMES = 3;

    private final AttributeKey<Integer> pingKey = AttributeKey.valueOf("attributeKeyPingCount");

    private ServerListener serverListener;

    public ServerAuthHandler(ServerListener serverListener) {
        this.serverListener = serverListener;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            String type = "";

            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                type = "Reader idle";
            } else if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                type = "Writer idle";
            } else if (idleStateEvent.state() == IdleState.ALL_IDLE) {
                type = "All idle";
            }

            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                Attribute<Integer> pingAttr = ctx.attr(pingKey);
                Integer pingCount = pingAttr.get();
                if (pingCount == null) {
                    pingCount = 1;
                } else {
                    pingCount++;
                }
                pingAttr.set(pingCount);

                if (pingCount > MAX_UN_REC_PING_TIMES) {
                    ctx.channel().close();
                    System.out.println("[" + ctx.channel().id().asLongText() + "] 心跳超时, 断开连接");
                }
            }

            System.out.println(ctx.channel().remoteAddress()+", 超时类型：" + type);

        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        super.channelRead(ctx, msg);
        Message.MessageBase message = (Message.MessageBase) msg;
        String clientId = ctx.channel().id().asLongText();
        System.out.println("[" + clientId + "], clientId = " + message.getClientId() +", message = " + message.getData());

        // 读取数据，清空心跳监控值
        Attribute<Integer> pingAttr = ctx.attr(pingKey);
        pingAttr.set(new Integer(0));

        Command.CommandType commandType = message.getCommandType();
        if (commandType.equals(Command.CommandType.AUTH)) {
            // 返回认证通过消息
            ctx.writeAndFlush(createMessage(message.getClientId(), Command.CommandType.AUTH_BACK, "Auth Back"));

            // 保存在线客户
            SessionManager.addOrReplace(clientId, ctx);

            this.serverListener.onClientConnected(clientId);

        } else if (commandType.equals(Command.CommandType.PING)) {
            ctx.writeAndFlush(createMessage(message.getClientId(), Command.CommandType.PONG, "pong"));

        } else {
            // 处理逻辑消息
            if (ctx.channel().isOpen()) {
                ctx.fireChannelRead(msg);
            }
        }
        ReferenceCountUtil.release(msg);
    }

    private Message.MessageBase createMessage(String clientId, Command.CommandType commandType, String data) {
        return Message.MessageBase.newBuilder()
                .setClientId(clientId)
                .setCommandType(commandType)
                .setData(data)
                .build();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        String clientId = ctx.channel().id().asLongText();
        System.out.println("[" + clientId + "], disconnected(inactive)");
        ctx.close();

        SessionManager.remove(clientId);

        this.serverListener.onClientDisconnected(clientId);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        String clientId = ctx.channel().id().asLongText();
        System.out.println("[" + clientId + "], disconnected(exceptionCaught)");
        ctx.close();

        SessionManager.remove(clientId);

        this.serverListener.onClientDisconnected(clientId);
    }
}
