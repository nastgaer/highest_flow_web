package highest.taolive.xdata.http;

public class RequestContext {

    private String host;

    private int port;

    private int corePoolSize = 4;

    private int maxPoolSize = 20;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int size) {
        this.corePoolSize = size;
    }

    public int getMaxPoolSize() {
        return this.maxPoolSize;
    }

    public void setMaxPoolSize(int size) {
        this.maxPoolSize = size;
    }
}
