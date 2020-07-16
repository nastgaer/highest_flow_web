package highest.flow.taobaolive.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.common.utils.PageUtils;
import highest.flow.taobaolive.sys.entity.SysLog;
import highest.flow.taobaolive.sys.entity.SysMember;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface LogService extends IService<SysLog> {

    PageUtils queryPage(PageParam pageParam);

}
