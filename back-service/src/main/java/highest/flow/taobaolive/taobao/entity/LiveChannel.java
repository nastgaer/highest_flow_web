package highest.flow.taobaolive.taobao.entity;

import lombok.Data;

import java.util.List;

@Data
public class LiveChannel {

    private int id;

    private String title;

    private String descInfo;

    private List<LiveColumn> columns;
}
