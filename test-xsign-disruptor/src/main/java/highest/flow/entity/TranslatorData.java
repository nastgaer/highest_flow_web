package highest.flow.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TranslatorData implements Serializable {

    private static final long serialVersionUID = 8763561286199081881L;

    private String productId;

    private int index = 0;

    private Map<String, Object> data = new HashMap<>();

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

}
