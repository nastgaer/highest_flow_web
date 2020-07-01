package highest.flow.taobaolive.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.sys.entity.PageEntity;
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
    public R logs(@RequestBody PageEntity pageEntity) {
        try {
            int pageNo = pageEntity.getPageNo();
            int pageSize = pageEntity.getPageSize();
            IPage<SysLog> page = this.logService.page(new Page<>((pageNo - 1) * pageSize, pageSize));
            List<SysLog> logs = page.getRecords();

            return R.ok().put("logs", logs).put("total_count", this.logService.size());

        } catch (Exception ex) {
            return R.error("获取系统记录失败");
        }
    }
}
