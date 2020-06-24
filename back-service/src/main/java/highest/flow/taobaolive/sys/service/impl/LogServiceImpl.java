package highest.flow.taobaolive.sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.sys.dao.SysLogDao;
import highest.flow.taobaolive.sys.entity.SysLog;
import highest.flow.taobaolive.sys.service.LogService;
import org.springframework.stereotype.Service;

@Service("sysLogService")
public class LogServiceImpl extends ServiceImpl<SysLogDao, SysLog> implements LogService {

}
