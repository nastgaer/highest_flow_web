package highest.flow.taobaolive.sys.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import highest.flow.taobaolive.sys.dao.MemberDao;
import highest.flow.taobaolive.sys.defines.MemberState;
import highest.flow.taobaolive.sys.entity.SysMember;
import highest.flow.taobaolive.sys.entity.SysMemberRole;
import highest.flow.taobaolive.sys.entity.SysMemberRoleGroup;
import highest.flow.taobaolive.sys.service.MemberRoleGroupService;
import highest.flow.taobaolive.sys.service.MemberRoleService;
import org.apache.commons.lang.RandomStringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.sys.service.MemberService;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, SysMember> implements MemberService {

    @Autowired
    private MemberRoleService memberRoleService;

    @Autowired
    private MemberRoleGroupService memberRoleGroupService;

    @Override
    public SysMember register(String username, String password, String mobile, String comment, List<String> roles, int state) {
        SysMember member = new SysMember();

        member.setUsername(username);
        member.setPassword(password);
        member.setMobile(mobile);
        member.setComment(comment);
        member.setState(MemberState.Normal.getState());
        member.setCreatedTime(new Date());
        member.setUpdatedTime(new Date());

        // sha256加密
        String salt = RandomStringUtils.randomAlphanumeric(20);
        member.setPassword(new Sha256Hash(member.getPassword(), salt).toHex());
        member.setSalt(salt);

        this.save(member);

        SysMembeSysMember newMember = baseMapper.selectOne(Wrappers.<SysMember>lambdaQuery().eq(SysMember::getUsername, username));

        List<SysMemberRole> memberRoles = memberRoleService.list();
        for (String role : roles) {
            for (SysMemberRole memberRole : memberRoles) {
                if (role.compareTo(memberRole.getName()) == 0) {
                    SysMemberRoleGroup sysMemberRoleGroup = new SysMemberRoleGroup();
                    sysMemberRoleGroup.setMemberId(newMember.getId());
                    sysMemberRoleGroup.setRoleId(memberRole.getId());

                    memberRoleGroupService.save(sysMemberRoleGroup);
                    break;
                }
            }
        }

        return newMember;
    }

    @Override
    public SysMember getMemberByUsername(String username) {
        return baseMapper.selectOne(Wrappers.<SysMember>lambdaQuery().eq(SysMember::getUsername, username));
    }
}
