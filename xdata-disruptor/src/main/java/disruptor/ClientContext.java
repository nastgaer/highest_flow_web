package disruptor;

import java.io.Serializable;
import java.net.Socket;
import java.util.Map;

public class ClientContext implements Serializable {

    private Socket socket;

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}
