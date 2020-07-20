package highest.flow.taobaolive.taobao.service;

import highest.flow.taobaolive.taobao.defines.ProductSearchSortKind;
import highest.flow.taobaolive.taobao.entity.ProductCategory;
import highest.flow.taobaolive.taobao.entity.ProductChannel;
import highest.flow.taobaolive.taobao.entity.ProductEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProductSearchService {

    List<ProductChannel> getChannels();

    List<ProductCategory> getCategories(int channelId);

    ProductCategory getCategory(int channelId, int categoryId);

    /**
     * 采集快选商品
     * @param productCategory
     * @param sortKind
     * @param startAt
     * @return
     */
    List<ProductEntity> searchProducts(ProductCategory productCategory, ProductSearchSortKind sortKind,
                                       int startAt,
                                       int startPrice,
                                       int endPrice,
                                       int minSales,
                                       boolean isTmall);
}
