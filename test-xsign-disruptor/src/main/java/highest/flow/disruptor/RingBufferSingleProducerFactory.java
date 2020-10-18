package highest.flow.disruptor;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import highest.flow.entity.TranslatorData;

import java.util.concurrent.Executors;

public class RingBufferSingleProducerFactory {

    public static RingBufferSingleProducerFactory getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        static final RingBufferSingleProducerFactory instance = new RingBufferSingleProducerFactory();
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

    public RingBuffer<TranslatorData> getRingBuffer() {
        return disruptor == null ? null : disruptor.getRingBuffer();
    }

    public void initAndStart(int bufferSize, WaitStrategy waitStrategy, EventHandler eventHandler) {
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

        disruptor.handleExceptionsWith(new EventExceptionHandler());
        disruptor.handleEventsWith(eventHandler);

        disruptor.start();
    }
}
