package highest.flow.taobaolive.sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.sys.dao.MemberRoleDao;
import highest.flow.taobaolive.sys.entity.SysMemberRole;
import highest.flow.taobaolive.sys.service.MemberRoleService;
import org.springframework.stereotype.Service;

@Service("memberRoleService")
public class MemberRoleServiceImpl extends ServiceImpl<MemberRoleDao, SysMemberRole> implements MemberRoleService {

}
