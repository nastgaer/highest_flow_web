package disruptor;

public interface OperationListener {

    void doOperate(ClientContext clientContext);
}
