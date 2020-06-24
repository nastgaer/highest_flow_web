package highest.flow.taobaolive.sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.sys.dao.MemberRoleDao;
import highest.flow.taobaolive.sys.dao.MemberRoleGroupDao;
import highest.flow.taobaolive.sys.entity.SysMemberRole;
import highest.flow.taobaolive.sys.entity.SysMemberRoleGroup;
import highest.flow.taobaolive.sys.service.MemberRoleGroupService;
import highest.flow.taobaolive.sys.service.MemberRoleService;
import org.springframework.stereotype.Service;

@Service("memberRoleGroupService")
public class MemberRoleGroupServiceImpl extends ServiceImpl<MemberRoleGroupDao, SysMemberRoleGroup> implements MemberRoleGroupService {

}
