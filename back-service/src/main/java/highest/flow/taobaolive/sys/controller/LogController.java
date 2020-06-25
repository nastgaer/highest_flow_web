package highest.flow.taobaolive.sys.controller;

import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.open.sys.PageParam;
import highest.flow.taobaolive.sys.entity.SysLog;
import highest.flow.taobaolive.sys.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sys")
public class LogController {

    @Autowired
    private LogService logService;

    @PostMapping("/logs")
    public R logs(@RequestBody PageParam pageParam) {
        try {
            List<SysLog> logs = this.logService.list();

            return R.ok().put("logs", logs).put("total_count", logs.size());

        } catch (Exception ex) {
            return R.error("获取系统记录失败");
        }
    }
}