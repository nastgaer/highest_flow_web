package highest.flow.taobaolive.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.common.utils.CommonUtils;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.common.utils.PageUtils;
import highest.flow.taobaolive.common.utils.Query;
import highest.flow.taobaolive.security.defines.LicenseCodeType;
import highest.flow.taobaolive.sys.dao.MemberDao;
import highest.flow.taobaolive.sys.defines.MemberLevel;
import highest.flow.taobaolive.sys.defines.MemberRole;
import highest.flow.taobaolive.sys.defines.MemberServiceType;
import highest.flow.taobaolive.sys.defines.MemberState;
import highest.flow.taobaolive.sys.entity.SysMember;
import highest.flow.taobaolive.sys.service.MemberRoleGroupService;
import org.apache.commons.lang.RandomStringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.sys.service.MemberService;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, SysMember> implements MemberService {

    @Autowired
    private MemberRoleGroupService memberRoleGroupService;

    @Override
    public PageUtils queryPage(PageParam pageParam) {
        int pageNo = pageParam.getPageNo();
        int pageSize = pageParam.getPageSize();
        String keyword = pageParam.getKeyword();

        Map<String, Object> params = new HashMap<>();
        params.put(Query.PAGE, pageNo);
        params.put(Query.LIMIT, pageSize);

        QueryWrapper<SysMember> queryWrapper = new QueryWrapper<>();
        if (!HFStringUtils.isNullOrEmpty(keyword)) {
            queryWrapper.like("member_name", keyword).or()
                    .like("comment", keyword);
        }
        queryWrapper.ne("level", MemberLevel.TestUser.getLevel())
                .ne("level", MemberLevel.LicenseUser.getLevel());

        IPage<SysMember> page = this.page(new Query<SysMember>().getPage(params), queryWrapper);
        return new PageUtils<SysMember>(page);
    }

    @Override
    public SysMember register(String memberName, String password, String mobile, String comment, List<String> roles, int level, int state) {
        SysMember member = new SysMember();

        member.setMemberName(memberName);
        member.setPassword(password);
        member.setMobile(mobile);
        member.setComment(comment);
        member.setLevel(level);
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
            if (!HFStringUtils.isNullOrEmpty(password)) {
                member.setPassword(password);
            }
            member.setMobile(mobile);
            member.setComment(comment);
            member.setState(state);
            member.setUpdatedTime(new Date());

            // sha256加密
            if (!HFStringUtils.isNullOrEmpty(password)) {
                String salt = RandomStringUtils.randomAlphanumeric(20);
                member.setPassword(new Sha256Hash(member.getPassword(), salt).toHex());
                member.setSalt(salt);
            }

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

            return true;

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

    @Override
    public SysMember createLicenseMember(LicenseCodeType licenseCodeType, MemberServiceType memberServiceType) {
        try {
            String memberName = (licenseCodeType == LicenseCodeType.Test ? "test" : "lic") +
                    CommonUtils.randomAlphabetic(6);
            String password = (licenseCodeType == LicenseCodeType.Test ? "testp" : "licp") +
                    CommonUtils.randomAlphabetic(6);

            List<String> roles = new ArrayList<>();
            if (memberServiceType == MemberServiceType.Ranking) {
                roles.add(MemberRole.ranking.toString());
            }
            if (memberServiceType == MemberServiceType.TaobaoLive) {
                roles.add(MemberRole.tblive.toString());
            }

            SysMember sysMember = this.register(memberName, password, "", "",
                    roles,
                    licenseCodeType == LicenseCodeType.Test ? MemberLevel.TestUser.getLevel() : MemberLevel.LicenseUser.getLevel(),
                    MemberState.Normal.getState());

            return sysMember;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
