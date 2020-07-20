package highest.flow.taobaolive.taobao.entity;

import lombok.Data;

@Data
public class ProductCategory {

    private int parentId;

    private int id;

    private int kxuanId;

    private int kxuanSwyt;

    private int categoryId;

    private int swytFilter;

    private boolean c2cRule;

    private String keyword;

    private String title;
}
