package com.useful.server;

import com.useful.server.session.SessionManager;
import com.useful.server.typeServer.TCPServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource(value = "classpath:application.properties")
public class ServerApplication {

    static final int PORT = Integer.parseInt(System.getProperty("port", "9090"));

    public static void main(String[] args) {
        try {
            ConfigurableApplicationContext context = SpringApplication.run(ServerApplication.class, args);

            // 开启监控线程
            final Thread monitor = new Thread(new Runnable() {
                public void run() {
                    while(true) {
                        try {
                            Thread.sleep(1000);

                            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> SESSION COUNT: " + SessionManager.getCount());

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
            monitor.start();

            TCPServer tcpServer = context.getBean(TCPServer.class);

            // 结束时全部关闭Socket
            Runtime.getRuntime().addShutdownHook(new Thread() {

                @Override
                public void run() {
                    super.run();

                    try {
                        if (monitor.isAlive()) {
                            monitor.interrupt();
                        }

                        tcpServer.stop();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            tcpServer.start();
            System.out.println("已经开启服务器, 127.0.0.1:8228");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
