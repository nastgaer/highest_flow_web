package xdata;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import disruptor.MessageConsumer;
import disruptor.MessageConsumerImpl;
import disruptor.RingBufferSingleProducerFactory;
import disruptor.http.HttpConfiguration;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import mina.service.MinaService;
import org.slf4j.LoggerFactory;
import disruptor.http.ListenThread;

import java.util.concurrent.atomic.AtomicLong;

public class XdataDisruptorApplication {

    public static org.slf4j.Logger logger = LoggerFactory.getLogger(XdataDisruptorApplication.class);

    public static AtomicLong success = new AtomicLong();
    public static AtomicLong failed = new AtomicLong();

    /**
     * Bootup the server from the console interface. This is very simple - it
     * just creates a new instance of <code>Tornado</code> and starts it.
     *
     */
    public static void main(String[] args) {
        Log4jConfiguration.configure("xdata.log");

        int defaultPort = 7227;
        int minaPort = defaultPort;

        ArgumentsParser.parse(args);

        /// start mornitor thread
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

        /// start mina service
        if (ArgumentsParser.hasArgument("mina.port")) {
            String port =(String) ArgumentsParser.get("mina.port");
            minaPort = Integer.parseInt(port);
        }

        MinaService.start(minaPort);

        /// start disruptor
        int consumerCount = 10;
        if (ArgumentsParser.hasArgument("consumers")) {
            consumerCount = Integer.parseInt((String) ArgumentsParser.get("consumers"));
        }

        MessageConsumer [] consumers = new MessageConsumer[consumerCount];
        for (int idx = 0; idx < consumerCount; idx++) {
            String consumerId = "consumer" + idx;
            consumers[idx] = new MessageConsumerImpl(consumerId);
        }

        RingBufferSingleProducerFactory.getInstance().initAndStart(
                1024 * 1024,
                new BusySpinWaitStrategy(), // new YieldingWaitStrategy(),
                consumers
        );

        /// http configuration
        String documentRoot = "./";
        if (ArgumentsParser.hasArgument("root")) {
            documentRoot = ArgumentsParser.get("root");
        }
        HttpConfiguration.setDocumentRoot(documentRoot);

        int httpPort = 9090;
        if (ArgumentsParser.hasArgument("http.port")) {
            String port =(String) ArgumentsParser.get("http.port");
            httpPort = Integer.parseInt(port);
        }

        /// start tornado
        ListenThread listenThread = new ListenThread(httpPort);
        listenThread.start();

        try {
            System.in.read();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
