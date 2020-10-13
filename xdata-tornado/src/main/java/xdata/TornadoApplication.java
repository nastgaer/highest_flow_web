package xdata;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import mina.service.MinaService;
import org.slf4j.LoggerFactory;
import tornado.Tornado;

import java.net.URL;
import java.net.URLDecoder;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TornadoApplication {

    public static org.slf4j.Logger logger = LoggerFactory.getLogger(TornadoApplication.class);

    public static AtomicLong success = new AtomicLong();
    public static AtomicLong failed = new AtomicLong();

    /**
     * Bootup the server from the console interface. This is very simple - it
     * just creates a new instance of <code>Tornado</code> and starts it.
     *
     * @see #Tornado(String[])
     */
    public static void main(String[] args) {
        int defaultPort = 7227;
        int minaPort = defaultPort;

//        // DEBUG
//        URL resourcePath = Tornado.class.getClassLoader().getResource("conf/");
//        String filePath = URLDecoder.decode(resourcePath.getPath());
//        if (filePath.startsWith("/")) {
//            filePath = filePath.substring(1).trim();
//        }
//        args = new String[] {"-c" + filePath};

        final OptionParser parser = new OptionParser("c:p:");
        final OptionSet commandLineOptions = parser.parse(args);

        if (commandLineOptions.hasArgument("p")) {
            String port =(String) commandLineOptions.valueOf("p");
            minaPort = Integer.parseInt(port);
        }

        MinaService.start(minaPort);

        // Start mornitor thread
        Thread monitor = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        logger.info("SUCCESS = " + success.get() + ", FAILED = " + failed.get());

                        Thread.sleep(10 * 1000);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        monitor.start();

        final Tornado server = new Tornado(commandLineOptions);
        if (!server.execute()) {
            MinaService.stop();
        }
    }
}
