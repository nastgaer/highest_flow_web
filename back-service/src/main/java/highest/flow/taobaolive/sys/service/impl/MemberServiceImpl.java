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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, SysMember> implements MemberService {

    @Autowired
    private MemberRoleGroupService memberRoleGroupService;

    @Override
    public SysMember register(String memberName, String password, String mobile, String comment, List<String> roles, int state) {
        SysMember member = new SysMember();

        member.setMemberName(memberName);
        member.setPassword(password);
        member.setMobile(mobile);
        member.setComment(comment);
        member.setState(state);
        member.setCreatedTime(new Date());
        member.setUpdatedTime(new Date());

        // sha256加密
        String salt = RandomStringUtils.randomAlphanumeric(20);
        member.setPassword(new Sha256Hash(member.getPassword(), salt).toHex());
        member.setSalt(salt);

        this.save(member);
        memberRoleGroupService.saveOrUpdate(member.getId(), roles);

        return member;
    }

    @Override
    public boolean update(int id, String memberName, String password, String mobile, String comment, List<String> roles, int state) {
        try {
            SysMember member = this.getById(id);

            if (member == null) {
                return false;
            }

            SysMember memberOther = this.getOne(Wrappers.<SysMember>lambdaQuery().eq(SysMember::getMemberName, memberName));
            if (memberOther.getId() != member.getId()) {
                return false;
            }

            member.setMemberName(memberName);
            member.setPassword(password);
            member.setMobile(mobile);
            member.setComment(comment);
            member.setState(state);
            member.setUpdatedTime(new Date());

            // sha256加密
            String salt = RandomStringUtils.randomAlphanumeric(20);
            member.setPassword(new Sha256Hash(member.getPassword(), salt).toHex());
            member.setSalt(salt);

            this.updateById(member);
            memberRoleGroupService.saveOrUpdate(member.getId(), roles);

            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteBatch(List<Integer> ids) {
        try {
            for (Integer id : ids) {
                this.memberRoleGroupService.deleteRole(id);
            }
            this.removeByIds(ids);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public SysMember getMemberByName(String memberName) {
        return baseMapper.selectOne(Wrappers.<SysMember>lambdaQuery().eq(SysMember::getMemberName, memberName));
    }

    @Override
    public List<String> getRoles(SysMember sysMember) {
        return this.memberRoleGroupService.queryRoleList(sysMember.getId());
    }
}
