package highest.flow.taobaolive.taobao.controller;

import highest.flow.taobaolive.common.annotation.SysLog;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.common.utils.ResourceReader;
import highest.flow.taobaolive.taobao.entity.ProductChannel;
import highest.flow.taobaolive.taobao.service.ProductSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PublicController {

    @Autowired
    private ProductSearchService productSearchService;

    @PostMapping("/v1.0/product/categories")
    public R getProductCategories() {
        try {
            List<ProductChannel> channels = productSearchService.getChannels();

            return R.ok().put("channels", channels);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }
}
