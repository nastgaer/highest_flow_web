package highest.flow.taobaolive.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.common.utils.PageUtils;
import highest.flow.taobaolive.sys.entity.SysLogEntity;
import org.springframework.stereotype.Service;

@Service
public interface SysLogService extends IService<SysLogEntity> {

    PageUtils queryPage(PageParam pageParam);

}
