package highest.flow.taolive.xdata;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;
import highest.flow.disruptor.*;
import highest.flow.entity.TranslatorData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

@SpringBootApplication
public class TestXsignDisruptorApplication implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(TestXsignDisruptorApplication.class, args);
    }

    @Value("${producers:100}")
    private int producerCount;

    @Value("${consumers:100}")
    private int consumerCount;

    @Value("${count:10000}")
    private int repeatCount;

    @Value("${single:true}")
    private boolean single;

    @Value("${sign.url:http://localhost:9090/xdata}")
    private String signUrl = "";

    private AtomicLong success = new AtomicLong(0),
                    fail = new AtomicLong(0);

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("URL = " + signUrl);

        testHttp();
    }

    public void testHttp() {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("utdid", "");
            map.put("uid", "");
            map.put("appkey", "25443018");
            map.put("sid", "");
            map.put("ttid", "10005533@taobaolive_android_1.8.4");
            map.put("pv", "6.3");
            map.put("devid", "");
            map.put("location1", "");
            map.put("location2", "");
            map.put("features", "27");
            map.put("subUrl", "mtop.taobao.sharepassword.querypassword");
            map.put("urlVer", "1.0");
            map.put("timestamp", 1599203806);
            map.put("data", "{\"passwordContent\":\"￥YIJNcWAbEW3￥\"}");

            CountDownLatch countDownLatch = new CountDownLatch(repeatCount);

            long startTime = System.currentTimeMillis();

            if (!single) {
                /// Multiple producers, multiple consumers
                MessageConsumer [] consumers = new MessageConsumer[consumerCount];
                for (int idx = 0; idx < consumerCount; idx++) {
                    String consumerId = "consumer" + idx;
                    MessageConsumerImpl consumer = new MessageConsumerImpl(consumerId);
                    consumer.setSignUrl(signUrl);
                    consumer.setAtomic(success, fail);
                    consumer.setCountDownLatch(countDownLatch);

                    consumers[idx] = consumer;
                }

//                RingBufferWorkerPoolFactory.getInstance().initAndStart(
//                        ProducerType.MULTI,
//                        1024 * 1024,
//                        new YieldingWaitStrategy(),
//                        consumers
//                );
//
//                for (int idx = 0; idx < producerCount; idx++) {
//                    String producerId = "producer" + idx;
//                    MessageProducer producer =
//                            RingBufferWorkerPoolFactory.getInstance().getMessageProducer(producerId);
//
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            for (int repeat = 0; repeat < repeatCount / producerCount; repeat++) {
//                                producer.product(repeat, map);
//                            }
//                        }
//                    }).start();
//                }

                /// Single producer, multiple consumers
                RingBufferSingleProducerFactory2.getInstance().initAndStart(
                        1024 * 1024,
                        new YieldingWaitStrategy(),
                        consumers
                );

                RingBuffer<TranslatorData> ringBuffer = RingBufferSingleProducerFactory2.getInstance().getRingBuffer();
                for (int repeat = 0; repeat < repeatCount; repeat++) {
                    final int index = repeat;
                    ringBuffer.publishEvent((event, sequence, data) -> {
                        TranslatorData translatorData = ringBuffer.get(sequence);
                        translatorData.setProductId("producer");
                        translatorData.setIndex(index);
                        translatorData.setData(map);
                    });
                }

            } else {
                // Single producer, single consumer
                TranslaterDataHandler translateDataHandler = new TranslaterDataHandler();
                translateDataHandler.setAtomic(success, fail);
                translateDataHandler.setSignUrl(signUrl);
                translateDataHandler.setCountDownLatch(countDownLatch);

                RingBufferSingleProducerFactory.getInstance().initAndStart(
                        1024 * 1024,
                        new YieldingWaitStrategy(),
                        translateDataHandler
                );

                RingBuffer<TranslatorData> ringBuffer = RingBufferSingleProducerFactory.getInstance().getRingBuffer();
                for (int repeat = 0; repeat < repeatCount; repeat++) {
                    final int index = repeat;
                    ringBuffer.publishEvent((event, sequence, data) -> {
                        TranslatorData translatorData = ringBuffer.get(sequence);
                        translatorData.setProductId("producer");
                        translatorData.setIndex(index);
                        translatorData.setData(map);
                    });
                }
            }

            countDownLatch.await();

            long times = System.currentTimeMillis() - startTime;
            System.out.println("【线程】总共耗时：" + times + "毫秒, SUCCESS: " + success + ", FAIL: " + fail);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
