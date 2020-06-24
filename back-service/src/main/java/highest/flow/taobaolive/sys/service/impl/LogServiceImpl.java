package highest.flow.taobaolive.sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.sys.dao.LogDao;
import highest.flow.taobaolive.sys.dao.MemberRoleDao;
import highest.flow.taobaolive.sys.entity.SysLog;
import highest.flow.taobaolive.sys.entity.SysMemberRole;
import highest.flow.taobaolive.sys.service.LogService;
import highest.flow.taobaolive.sys.service.MemberRoleService;
import org.springframework.stereotype.Service;

@Service("sysLogService")
public class LogServiceImpl extends ServiceImpl<LogDao, SysLog> implements LogService {

}
