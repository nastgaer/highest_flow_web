package highest.flow.taobaolive.mina;

import highest.flow.taobaolive.common.config.ConnectionConfig;
import highest.flow.taobaolive.mina.filter.ICommandFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

@Service
public class ClientService {

    private ConnectionConfig connectionConfig;

    private List<ICommandFilter> filters = new ArrayList<>();

    private InetSocketAddress inetSocketAddress;

    private NioSocketConnector nioSocketConnector;

    public void start(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
        initialize();
    }

    private void initialize() {
        inetSocketAddress = new InetSocketAddress(connectionConfig.getIp(), )

    }

    private boolean connect() {

    }

    public ClientService addFilter(ICommandFilter filter) {
        return this;
    }
}
