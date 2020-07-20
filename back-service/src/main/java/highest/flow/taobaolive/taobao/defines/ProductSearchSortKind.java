package highest.flow.taobaolive.taobao.defines;

public enum ProductSearchSortKind {

    SortByDefault(0), // 综合排序
    SortBySales(1); // 销量排序

    private int kind = 0;

    ProductSearchSortKind(int kind) {
        this.kind = kind;
    }

    public int getKind() {
        return kind;
    }

    public static ProductSearchSortKind from(int value) {
        if (SortByDefault.getKind() == value) {
            return SortByDefault;
        }
        return SortBySales;
    }
}
