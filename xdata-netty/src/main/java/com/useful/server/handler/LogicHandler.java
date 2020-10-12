package com.useful.server.handler;

import com.useful.common.protobuf.Command;
import com.useful.common.protobuf.Message;
import com.useful.server.listener.ServerListener;
import io.netty.channel.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;

public class LogicHandler extends ChannelInboundHandlerAdapter {

    private final AttributeKey<String> attributeKey = AttributeKey.valueOf("clientInfo");

    private ServerListener serverListener;

    public LogicHandler(ServerListener serverListener) {
        this.serverListener = serverListener;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        super.channelRead(ctx, msg);
        final Message.MessageBase message = (Message.MessageBase) msg;
        String clientId = ctx.channel().id().asLongText();

        this.serverListener.onMessage(clientId, message);

//        if (message.getCommandType() == Command.CommandType.ECHO) {
//            ChannelFuture channelFuture = ctx.writeAndFlush(Message.MessageBase.newBuilder()
//                    .setClientId(message.getClientId())
//                    .setCommandType(Command.CommandType.ECHO_BACK)
//                    .setData("[ECHO BACK]" + message.getData())
//            );
//
//            channelFuture.addListener(new ChannelFutureListener() {
//                public void operationComplete(ChannelFuture channelFuture) throws Exception {
//                    if (channelFuture.isSuccess()) {
//                        System.out.println("[ECHO COMPLETE] clientId = " + message.getClientId());
//                    }
//                }
//            });
//        } else if (message.getCommandType() == Command.CommandType.ECHO_BACK) {
//            // TODO
//        }

        ReferenceCountUtil.release(msg);
    }
}
