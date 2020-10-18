package highest.flow.taolive.xdata.http.httpclient;

import org.apache.http.conn.HttpClientConnectionManager;

import java.util.concurrent.TimeUnit;

public class HttpClientConnectionMonitorThread extends Thread {

    private final HttpClientConnectionManager httpClientConnectionManager;

    private volatile boolean shutdown;

    public HttpClientConnectionMonitorThread(HttpClientConnectionManager httpClientConnectionManager) {
        super();
        this.setName("http-connection-monitor");
        this.setDaemon(true);
        this.httpClientConnectionManager = httpClientConnectionManager;
        this.start();
    }

    @Override
    public void run() {
        try {
            while (!shutdown) {
                synchronized (this) {
                    wait(5000); // 等待5秒
                    // 关闭过期的连接
                    httpClientConnectionManager.closeExpiredConnections();
                    // 选择关闭空闲30秒的连接
                    httpClientConnectionManager.closeIdleConnections(30, TimeUnit.SECONDS);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void shutdownMonitor() {
        synchronized (this) {
            shutdown = true;
            notifyAll();
        }
    }
}
