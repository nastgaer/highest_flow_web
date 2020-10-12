package com.useful.server.listener.impl;

import com.useful.common.protobuf.Command;
import com.useful.common.protobuf.Message;
import com.useful.server.listener.ServerListener;
import com.useful.server.session.SessionManager;
import com.useful.server.sync.PromiseFuture;
import com.useful.server.sync.WaitingPool;
import io.netty.channel.ChannelHandlerContext;
import com.google.protobuf.util.JsonFormat;

public class ServerListenerImpl implements ServerListener {

    private final int repeatCount = 500;

    private Thread thread = null;

    public void onClientConnected(final String clientId) {

    }

    public void onClientDisconnected(String clientId) {
        try {
            if (thread.isAlive()) {
                thread.interrupt();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onMessage(String clientId, Message.MessageBase message) {
        try {
            JsonFormat.Printer printer = JsonFormat.printer();
            String json = printer.print(message);
            System.out.println("[" + clientId + "] " + json);

            long messageId = message.getMessageId();
            Command.CommandType commandType = message.getCommandType();
            json = message.getData();

            if (commandType == Command.CommandType.RESPONSE) {
                PromiseFuture<String> promiseFuture = WaitingPool.getInstance().get(messageId);
                if (promiseFuture != null) {
                    promiseFuture.set(json);

                    WaitingPool.getInstance().remove(messageId);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
