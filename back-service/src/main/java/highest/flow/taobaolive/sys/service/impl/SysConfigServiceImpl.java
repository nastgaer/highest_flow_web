package highest.flow.taobaolive.sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.sys.dao.SysConfigDao;
import highest.flow.taobaolive.sys.entity.SysConfigEntity;
import highest.flow.taobaolive.sys.service.SysConfigService;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service("sysConfigService")
public class SysConfigServiceImpl extends ServiceImpl<SysConfigDao, SysConfigEntity> implements SysConfigService {

    @Override
    public String getValue(String key) {
        return this.baseMapper.getConfValue(key);
    }
}
