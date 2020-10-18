package disruptor;

import disruptor.http.ServerThread;

import java.net.Socket;

public class MessageConsumerImpl extends MessageConsumer {

    public MessageConsumerImpl(String consumerId) {
        super(consumerId);
    }

    @Override
    public void onEvent(ClientContext clientContext) throws Exception {
        ServerThread serverThread = new ServerThread();

        Socket socket = clientContext.getSocket();
        serverThread.run(socket);
    }
}
