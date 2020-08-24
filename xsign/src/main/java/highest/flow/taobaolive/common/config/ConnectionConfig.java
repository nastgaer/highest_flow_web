package highest.flow.taobaolive.common.config;

import lombok.Data;

public class ConnectionConfig {

    private String ip;

    private int port;

    private int idleTimeout;

    private int heartBeatRate;

    private String getIp() {
        return ip;
    }

    private int getPort() {
        return port;
    }

    private int getIdleTimeout() {
        return idleTimeout;
    }

    private int getHeartBeatRate() {
        return heartBeatRate;
    }

    public static class Builder {

        private String ip = "127.0.0.1";

        private int port = 9995;

        private int idleTimeout = 30;

        private int heartBeatRate = 15;

        public void setIp(String ip) {
            this.ip = ip;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public void setIdleTimeout(int idleTimeout) {
            this.idleTimeout = idleTimeout;
        }

        public void setHeartBeatRate(int heartBeatRate) {
            this.heartBeatRate = heartBeatRate;
        }

        public ConnectionConfig build() {
            ConnectionConfig connectionConfig = new ConnectionConfig();
            connectionConfig.ip = this.ip;
            connectionConfig.port = this.port;
            connectionConfig.idleTimeout = this.idleTimeout;
            connectionConfig.heartBeatRate = this.heartBeatRate;

            return connectionConfig;
        }
    }

}
