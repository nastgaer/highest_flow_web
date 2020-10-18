package highest.flow.disruptor;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import highest.flow.entity.TranslatorData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RingBufferSingleProducerFactory2 {

    public static RingBufferSingleProducerFactory2 getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        static final RingBufferSingleProducerFactory2 instance = new RingBufferSingleProducerFactory2();
    }

    /**
     * 异常静态类
     *
     * @author Alienware
     */
    static class EventExceptionHandler implements ExceptionHandler<TranslatorData> {
        public void handleEventException(Throwable ex, long sequence, TranslatorData event) {
        }

        public void handleOnStartException(Throwable ex) {
        }

        public void handleOnShutdownException(Throwable ex) {
        }
    }

    private Disruptor<TranslatorData> disruptor;
    private RingBuffer<TranslatorData> ringBuffer;

    public RingBuffer<TranslatorData> getRingBuffer() {
        return disruptor == null ? null : disruptor.getRingBuffer();
    }

    public void initAndStart(int bufferSize, WaitStrategy waitStrategy, MessageConsumer[] consumers) {
//        ringBuffer = RingBuffer.createSingleProducer(new EventFactory<TranslatorData>() {
//            @Override
//            public TranslatorData newInstance() {
//                return new TranslatorData();
//            }
//        }, bufferSize, waitStrategy);
//
//        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//
//        for (int idx = 0; idx < consumerCount; idx++) {
//            SequenceBarrier sequenceBarrier = ringBuffer.newBarrier();
//
//            BatchEventProcessor<TranslatorData> transProcessor = new BatchEventProcessor<>(
//                    ringBuffer, sequenceBarrier, eventHandler
//            );
//
//            ringBuffer.addGatingSequences(transProcessor.getSequence());
//
//            executorService.submit(transProcessor);
//        }

        disruptor = new Disruptor<TranslatorData>(new EventFactory<TranslatorData>() {
            @Override
            public TranslatorData newInstance() {
                return new TranslatorData();
            }
        },
                bufferSize,
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()),
                ProducerType.SINGLE,
                waitStrategy);

        disruptor.handleExceptionsWith(new RingBufferSingleProducerFactory.EventExceptionHandler());
        disruptor.handleEventsWithWorkerPool(consumers);

        disruptor.start();
    }
}
