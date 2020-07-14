package highest.flow.taobaolive.sys.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.sys.dao.MemberRoleDao;
import highest.flow.taobaolive.sys.dao.MemberRoleGroupDao;
import highest.flow.taobaolive.sys.entity.SysMemberRole;
import highest.flow.taobaolive.sys.entity.SysMemberRoleGroup;
import highest.flow.taobaolive.sys.service.MemberRoleGroupService;
import highest.flow.taobaolive.sys.service.MemberRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("memberRoleGroupService")
public class MemberRoleGroupServiceImpl extends ServiceImpl<MemberRoleGroupDao, SysMemberRoleGroup> implements MemberRoleGroupService {

    @Autowired
    private MemberRoleService memberRoleService;

    @Override
    public void saveOrUpdate(int memberId, List<String> roles) {
        this.deleteRole(memberId);

        List<SysMemberRole> memberRoles = memberRoleService.list();
        for (String role : roles) {
            for (SysMemberRole memberRole : memberRoles) {
                if (role.compareTo(memberRole.getName()) == 0) {
                    SysMemberRoleGroup sysMemberRoleGroup = new SysMemberRoleGroup();
                    sysMemberRoleGroup.setMemberId(memberId);
                    sysMemberRoleGroup.setRoleId(memberRole.getId());

                    this.save(sysMemberRoleGroup);
                    break;
                }
            }
        }
    }

    @Override
    public List<String> queryRoleList(int memberId) {
        List<SysMemberRoleGroup> roleGroups = this.list(Wrappers.<SysMemberRoleGroup>lambdaQuery()
                .eq(SysMemberRoleGroup::getMemberId, memberId));
        List<String> roles = new ArrayList<>();
        for (SysMemberRoleGroup roleGroup : roleGroups) {
            SysMemberRole sysMemberRole = memberRoleService.getById(roleGroup.getRoleId());
            roles.add(sysMemberRole.getName());
        }
        return roles;
    }

    @Override
    public void deleteRole(int memberId) {
        this.remove(Wrappers.<SysMemberRoleGroup>lambdaQuery().eq(SysMemberRoleGroup::getMemberId, memberId));
    }
}
