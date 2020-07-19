package highest.flow.taobaolive.sys.controller;

import highest.flow.taobaolive.api.param.RankingQueryParam;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1.0/sys/dashboard")
public class DashboardController extends AbstractController {

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    @PostMapping("/tbacc")
    public R tbacc() {
        try {
            int normalCount = taobaoAccountService.getNormalCount();
            int expiredCount = taobaoAccountService.getExpiredCount();

            return R.ok().put("total_count", normalCount + expiredCount)
                    .put("normal_count", normalCount)
                    .put("expired_count", expiredCount);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @PostMapping("/ranking")
    public R ranking(@RequestBody RankingQueryParam param) {
        try {
            return R.ok().put("total_score", 0)
                    .put("total_score_double_buy", 0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @PostMapping("/live")
    public R live() {
        return R.ok().put("test_lives", 0)
                .put("fee_lives", 0)
                .put("fee_days", 0);
    }
}
