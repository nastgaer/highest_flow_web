package highest.flow.taobaolive.taobao.service;

import highest.flow.taobaolive.taobao.entity.ProductCategory;
import highest.flow.taobaolive.taobao.entity.ProductChannel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProductSearchService {

    List<ProductChannel> getChannels();

    List<ProductCategory> getCategories(int channelId);

    ProductCategory getCategory(int channelId, int categoryId);
}
