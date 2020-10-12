package com.useful.server.listener;

import com.useful.common.protobuf.Message;

public interface ServerListener {

    void onClientConnected(String clientId);

    void onClientDisconnected(String clientId);

    void onMessage(String clientId, Message.MessageBase message);
}
