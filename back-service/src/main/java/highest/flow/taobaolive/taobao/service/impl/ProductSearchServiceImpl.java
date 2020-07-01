package highest.flow.taobaolive.taobao.service.impl;

import highest.flow.taobaolive.common.utils.ResourceReader;
import highest.flow.taobaolive.taobao.entity.ProductCategory;
import highest.flow.taobaolive.taobao.entity.ProductChannel;
import highest.flow.taobaolive.taobao.service.ProductSearchService;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("productSearchServiceImpl")
public class ProductSearchServiceImpl implements ProductSearchService {

    private static List<ProductChannel> channels = new ArrayList<>();

    static {
        try {
            String text = ResourceReader.getProductCategoryList();

            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            List list = jsonParser.parseList(text);

            for (Object obj : list) {
                Map<String, Object> map = (Map<String, Object>) obj;
                String channelTitle = (String) map.get("title");
                int channelId = Integer.parseInt(String.valueOf(map.get("id")));
                List suggests = (List) map.get("suggests");

                List<ProductCategory> categories = new ArrayList<>();

                for (Object obj2 : suggests) {
                    Map<String, Object> mapSuggest = (Map<String, Object>) obj2;
                    int categoryId = Integer.parseInt(String.valueOf(mapSuggest.get("id")));
                    String categoryTitle = (String) mapSuggest.get("title");
                    int kxuanId = Integer.parseInt(String.valueOf(mapSuggest.get("kxuan_id")));
                    int kxuanSwytItem = Integer.parseInt(String.valueOf(mapSuggest.get("kxuan_swyt_item")));
                    int swytFilter = mapSuggest.containsKey("swyt_filter") ? Integer.parseInt(String.valueOf(mapSuggest.get("swyt_filter"))) : 0;
                    int kxuanCategoryId = mapSuggest.containsKey("cat") ? Integer.parseInt(String.valueOf(mapSuggest.get("cat"))) : 0;
                    String keyword = mapSuggest.containsKey("keyword") ? (String) mapSuggest.get("keyword") : "";

                    ProductCategory productCategory = new ProductCategory();
                    productCategory.setParentId(channelId);
                    productCategory.setId(categoryId);
                    productCategory.setTitle(categoryTitle);
                    productCategory.setKxuanId(kxuanId);
                    productCategory.setKxuanSwyt(kxuanSwytItem);
                    productCategory.setSwytFilter(swytFilter);
                    productCategory.setCategoryId(kxuanCategoryId);
                    productCategory.setKeyword(keyword);

                    categories.add(productCategory);
                }

                ProductChannel productChannel = new ProductChannel();
                productChannel.setId(channelId);
                productChannel.setTitle(title);
                productChannel.setCategories(categories);

                channels.add(productChannel);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public List<ProductChannel> getChannels() {
        return channels;
    }

    @Override
    public List<ProductCategory> getCategories(int channelId) {
        for (ProductChannel channel : channels) {
            if (channel.getId() == channelId) {
                return channel.getCategories();
            }
        }
        return null;
    }

    @Override
    public ProductCategory getCategory(int channelId, int categoryId) {
        List<ProductCategory> categories = getCategories(channelId);
        if (categories == null) {
            return null;
        }

        for (ProductCategory productCategory : categories) {
            if (productCategory.getId() == categoryId) {
                return productCategory;
            }
        }
        return null;
    }
}
