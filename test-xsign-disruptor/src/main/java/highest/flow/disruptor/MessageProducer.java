package highest.flow.disruptor;

import com.lmax.disruptor.RingBuffer;
import highest.flow.entity.TranslatorData;

import java.util.Map;

public class MessageProducer {

    private String producerId;

    private RingBuffer<TranslatorData> ringBuffer;

    public MessageProducer(String producerId, RingBuffer<TranslatorData> ringBuffer) {
        this.producerId = producerId;
        this.ringBuffer = ringBuffer;
    }

    public void product(int index, Map<String, Object> map) {
        long sequence = ringBuffer.next();
        try {
            TranslatorData data = ringBuffer.get(sequence);
            data.setProductId(producerId);
            data.setIndex(index);
            data.setData(map);

        } finally {
            ringBuffer.publish(sequence);
        }
    }

}
