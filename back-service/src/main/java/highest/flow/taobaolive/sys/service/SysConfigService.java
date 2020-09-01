package highest.flow.taobaolive.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.common.utils.PageUtils;
import highest.flow.taobaolive.sys.entity.SysConfigEntity;
import highest.flow.taobaolive.sys.entity.SysLogEntity;
import org.springframework.stereotype.Service;

@Service
public interface SysConfigService extends IService<SysConfigEntity> {

    public String getValue(String key);

}
