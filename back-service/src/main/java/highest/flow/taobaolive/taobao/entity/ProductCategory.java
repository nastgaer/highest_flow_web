package highest.flow.taobaolive.taobao.entity;

import highest.flow.taobaolive.common.utils.HFStringUtils;
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

    public boolean compareTo(ProductCategory productCategory) {
        if (this.getKxuanId() != productCategory.getKxuanId()) {
            return false;
        }
        if (this.getKxuanSwyt() != productCategory.getKxuanSwyt()) {
            return false;
        }
        if (this.getCategoryId() != productCategory.getCategoryId()) {
            return false;
        }
        if (this.getSwytFilter() != productCategory.getSwytFilter()) {
            return false;
        }
        if (this.isC2cRule() != productCategory.isC2cRule()) {
            return false;
        }
        if (this.getKeyword().compareTo(productCategory.getKeyword()) != 0) {
            return false;
        }
        return true;
    }
}
