package highest.flow.taobaolive.taobao.entity;

import lombok.Data;

import java.util.List;

@Data
public class ProductChannel {

    private int id;

    private String title;

    private List<ProductCategory> categories;
}
