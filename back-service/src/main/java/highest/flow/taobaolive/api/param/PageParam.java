package highest.flow.taobaolive.api.param;

import lombok.Data;

@Data
public class PageParam {

    private int pageNo;

    private int pageSize;

    private String keyword;
}
