package highest.flow.taobaolive.sys.controller;

import highest.flow.taobaolive.common.utils.PageUtils;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.sys.service.SysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1.0/sys")
public class LogController {

    @Autowired
    private SysLogService sysLogService;

    @PostMapping("/logs")
    public R logs(@RequestBody PageParam pageParam) {
        try {
            PageUtils pageUtils = this.sysLogService.queryPage(pageParam);

            return R.ok().put("logs", pageUtils.getList()).put("total_count", pageUtils.getTotalCount());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("获取系统记录失败");
    }
}
