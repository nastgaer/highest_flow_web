package highest.flow.disruptor;

import com.lmax.disruptor.WorkHandler;
import highest.flow.entity.TranslatorData;

/**
 * @author Alienware
 */
public abstract class MessageConsumer implements WorkHandler<TranslatorData> {

    protected String consumerId;

    public MessageConsumer(String consumerId) {
        this.consumerId = consumerId;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

}
