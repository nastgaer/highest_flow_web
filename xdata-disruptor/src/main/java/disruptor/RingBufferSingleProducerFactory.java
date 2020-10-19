package disruptor;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

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
    static class EventExceptionHandler implements ExceptionHandler<ClientContext> {
        public void handleEventException(Throwable ex, long sequence, ClientContext event) {
        }

        public void handleOnStartException(Throwable ex) {
        }

        public void handleOnShutdownException(Throwable ex) {
        }
    }

    private Disruptor<ClientContext> disruptor;

    public RingBuffer<ClientContext> getRingBuffer() {
        return disruptor == null ? null : disruptor.getRingBuffer();
    }

    public void initAndStart(int bufferSize, WaitStrategy waitStrategy, MessageConsumer[] consumers) {
        disruptor = new Disruptor<>(
                new EventFactory<ClientContext>() {

                    @Override
                    public ClientContext newInstance() {
                        return new ClientContext();
                    }
                },
                bufferSize,
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2),
                ProducerType.SINGLE,
                waitStrategy
        );

        disruptor.handleExceptionsWith(new EventExceptionHandler());
        disruptor.handleEventsWithWorkerPool(consumers);
        disruptor.start();
    }
}
