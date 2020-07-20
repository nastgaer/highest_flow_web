package highest.flow.taobaolive.taobao.service.impl;

import highest.flow.taobaolive.common.http.HttpHelper;
import highest.flow.taobaolive.common.http.Request;
import highest.flow.taobaolive.common.http.ResponseType;
import highest.flow.taobaolive.common.http.SiteConfig;
import highest.flow.taobaolive.common.http.cookie.DefaultCookieStorePool;
import highest.flow.taobaolive.common.http.httpclient.response.Response;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.common.utils.ResourceReader;
import highest.flow.taobaolive.taobao.defines.ProductSearchSortKind;
import highest.flow.taobaolive.taobao.entity.ProductCategory;
import highest.flow.taobaolive.taobao.entity.ProductChannel;
import highest.flow.taobaolive.taobao.entity.ProductEntity;
import highest.flow.taobaolive.taobao.service.ProductSearchService;
import org.apache.http.HttpStatus;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
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
                    productCategory.setC2cRule(false);
                    productCategory.setKeyword(keyword);

                    categories.add(productCategory);
                }

                ProductChannel productChannel = new ProductChannel();
                productChannel.setId(channelId);
                productChannel.setTitle(channelTitle);
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

    @Override
    public List<ProductEntity> searchProducts(ProductCategory productCategory, ProductSearchSortKind sortKind, int startAt,
                                              int startPrice,
                                              int endPrice,
                                              int minSales,
                                              boolean isTmall) {
        try {
            Map<String, Object> urlParams = new HashMap<>();

            urlParams.put("data-key", "s");
            urlParams.put("data-value", String.valueOf(startAt));
            urlParams.put("callback", "jsonp970");
            if (productCategory.getKeyword().length() > 0)
                urlParams.put("q", productCategory.getKeyword());
            urlParams.put("kxuan_swyt_item", String.valueOf(productCategory.getKxuanSwyt()));
            urlParams.put("ruletype", "0");
            urlParams.put("searchtype", "item");
            urlParams.put("uniq", "pid");
            urlParams.put("navigator", "all");
            urlParams.put("biz30day", String.valueOf(minSales));
            if (sortKind == ProductSearchSortKind.SortByDefault)
                urlParams.put("sort", "default");
            else
                urlParams.put("sort", "sale-desc");
            urlParams.put("id", String.valueOf(productCategory.getKxuanId()));
            urlParams.put("ie", "utf8");
            urlParams.put("is_spu", "0");
            urlParams.put("bcoffset", "2");
            urlParams.put("ntoffset", "0");
            urlParams.put("nested", "we");
            urlParams.put("fs", "1");
            urlParams.put("s", "0");  // start count
            if (productCategory.getCategoryId() > 0)
                urlParams.put("cat", String.valueOf(productCategory.getCategoryId()));
            urlParams.put("enginetype", "0");
            if (isTmall)
                urlParams.put("user_type", "1");
            if (startPrice >= 0 && endPrice > 0 && startPrice < endPrice)
                urlParams.put("filter", "reserve_price[" + startPrice + "," + endPrice + "]");
            if (productCategory.getSwytFilter() > 0)
                urlParams.put("swyt-filter", String.valueOf(productCategory.getSwytFilter()));
            if (productCategory.isC2cRule())
                urlParams.put("is_cp_rules_c2c", "1");

            String url = "https://kxuan.taobao.com/searchSp.htm?";

            for (String key : urlParams.keySet()) {
                url += key + "=" + URLEncoder.encode(String.valueOf(urlParams.get(key))) + "&";
            }

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36")
                            .setContentType("application/json"),
                    new Request("GET", url, ResponseType.TEXT));

            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return null;
            }

            String respText = response.getResult();
            respText = respText.substring("jsonp970(".length(), respText.length() - "jsonp970(".length() - 1);

            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            Map<String, Object> mapModes = (Map<String, Object>) map.get("mods");
            Map<String, Object> mapPager = (Map<String, Object>) mapModes.get("pages");
            Map<String, Object> mapPagerData = (Map<String, Object>) mapPager.get("data");

            int totalCount = Integer.parseInt(String.valueOf(mapPagerData.get("totalCount")));

            Map<String, Object> mapItemList = (Map<String, Object>) mapModes.get("itemlist");
            Map<String, Object> mapData = (Map<String, Object>) mapItemList.get("data");
            List lstAuctions = (List) mapData.get("auctions");

            List<ProductEntity> productEntities = new ArrayList<>();
            for (Object obj : lstAuctions) {
                Map<String, Object> mapAuction = (Map<String, Object>) obj;

                ProductEntity productEntity = new ProductEntity();
                productEntity.setCategoryId(String.valueOf(mapAuction.get("category")));
                productEntity.setCategoryTitle(productCategory.getTitle());
                productEntity.setProductId(String.valueOf(mapAuction.get("nid")));
                productEntity.setUrl(String.valueOf(mapAuction.get("detail_url")));
                productEntity.setTitle(String.valueOf(mapAuction.get("title")));
                productEntity.setPicurl(String.valueOf(mapAuction.get("pic_url")));
                productEntity.setPrice(String.valueOf(mapAuction.get("view_price")));
                productEntity.setShopName(String.valueOf(mapAuction.get("nick")));
                // productEntity.setLocation(String.valueOf(mapAuction.get("item_loc")));
                productEntity.setTitle(productEntity.getTitle().replace("<span class=H>", "").replace("</span>", ""));

                productEntities.add(productEntity);
            }

            return productEntities;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
