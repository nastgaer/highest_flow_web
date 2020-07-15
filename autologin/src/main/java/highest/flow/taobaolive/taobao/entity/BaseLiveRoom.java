package highest.flow.taobaolive.taobao.entity;

import lombok.Data;

@Data
public class BaseLiveRoom extends PreLiveRoomSpec {

    private int pscChannelId;

    private int pscCategoryId;

    private int pscStartPrice;

    private int pscEndPrice;

    private int pscMinSales;

    private int pscProductCount;

    private boolean pscIsTmall;

    /**
     * ProductSearchSortKind
     */
    private int pscSortKind;
}
