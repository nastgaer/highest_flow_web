package highest.flow.taobaolive.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import highest.flow.taobaolive.api.param.*;
import highest.flow.taobaolive.common.annotation.SysLog;
import highest.flow.taobaolive.common.config.Config;
import highest.flow.taobaolive.common.utils.CommonUtils;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.common.utils.PageUtils;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.security.defines.LicenseCodeState;
import highest.flow.taobaolive.security.defines.LicenseCodeType;
import highest.flow.taobaolive.security.entity.LicenseCode;
import highest.flow.taobaolive.security.service.LicenseCodeService;
import highest.flow.taobaolive.sys.defines.MemberLevel;
import highest.flow.taobaolive.sys.defines.MemberRole;
import highest.flow.taobaolive.sys.defines.MemberServiceType;
import highest.flow.taobaolive.sys.entity.*;
import highest.flow.taobaolive.sys.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/v1.0/sys")
public class MemberController extends AbstractController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private LicenseCodeService licenseCodeService;

    @SysLog("注册会员")
    @PostMapping("/register")
    public R register(@RequestBody RegisterMemberParam registerMemberParam) {
        try {
            SysMember sysMember = memberService.getMemberByName(registerMemberParam.getMemberName());
            if (sysMember != null) {
                return R.error("已经注册的会员");
            }

            List<String> roles = registerMemberParam.getRoles();
            int level = MemberLevel.Administrator.getLevel();

            for (MemberRole memberRole : MemberRole.values()) {
                if (!roles.contains(memberRole.toString())) {
                    level = MemberLevel.Normal.getLevel();
                    break;
                }
            }

            sysMember = memberService.register(registerMemberParam.getMemberName(),
                    registerMemberParam.getPassword(),
                    registerMemberParam.getMobile(),
                    registerMemberParam.getComment(),
                    roles,
                    level,
                    registerMemberParam.getState());

            if (sysMember == null) {
                return R.error("注册用户失败");
            }

            return R.ok().put("member_id", sysMember.getId());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("注册用户失败");
    }

    @SysLog("会员列表")
    @PostMapping("/list")
    public R list(@RequestBody PageParam pageParam) {
        try {
            PageUtils pageUtils = this.memberService.queryPage(pageParam);

            List<SysMember> members = pageUtils.getList();

            for (SysMember sysMember : members) {
                List<String> roles = memberService.getRoles(sysMember);
                sysMember.setRoles(roles);
            }

            return R.ok().put("users", members).put("total_count", pageUtils.getTotalCount());

        } catch (Exception ex){
            ex.printStackTrace();
        }
        return R.error("获取会员列表失败");
    }

    @SysLog("更新会员信息")
    @PostMapping("/update")
    public R update(@RequestBody UpdateMemberParam updateMemberParam) {
        try {
            SysMember sysMember = memberService.getMemberByName(updateMemberParam.getMemberName());
            if (sysMember == null) {
                return R.error("已经注册的会员");
            }

            SysMember memberOther = memberService.getMemberByName(updateMemberParam.getMemberName());
            if (memberOther.getId() != sysMember.getId()) {
                return R.error("已经注册的会员名称");
            }

            if (this.memberService.update(updateMemberParam.getId(),
                    updateMemberParam.getMemberName(),
                    updateMemberParam.getPassword(),
                    updateMemberParam.getMobile(),
                    updateMemberParam.getComment(),
                    updateMemberParam.getRoles(),
                    updateMemberParam.getState())) {
                return R.ok();
            }

            return R.error("更新会员信息失败");

        } catch (Exception ex){
            ex.printStackTrace();
        }
        return R.error("更新会员信息失败");
    }

    @SysLog("批量删除会员")
    @PostMapping("/batch_delete")
    public R batchDelete(@RequestBody IdsParam idsParam) {
        try {
            // 获取管理员的id
            SysMember administrator = this.memberService.getMemberByName(Config.ADMINISTRATOR);

            List<Integer> newIds = new ArrayList<>();
            for (Integer id : idsParam.getIds()) {
                if (id == administrator.getId()) {
                    continue;
                }

                newIds.add(id);
            }

            if (this.memberService.deleteBatch(newIds)) {
                return R.ok();
            }

            return R.error("批量删除会员失败");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("批量删除会员失败");
    }

    @SysLog("注册软件代码")
    @PostMapping("/add_internal_code")
    public R addInternalCode(@RequestBody AddNewLicenseCodeParam internalCodeParam) {
        try {
            String code = "internal" + CommonUtils.randomAlphabetic(8);

            LicenseCode licenseCode = new LicenseCode();
            licenseCode.setCodeType(internalCodeParam.getLicenseCodeType());
            licenseCode.setServiceType(internalCodeParam.getServiceType());
            licenseCode.setHours(internalCodeParam.getHours());
            licenseCode.setCode(code);
            licenseCode.setState(LicenseCodeState.Created.getState());
            licenseCode.setCreatedTime(new Date());

            Date expires = CommonUtils.addHours(new Date(), internalCodeParam.getHours());

            licenseCodeService.save(licenseCode);

            return R.ok()
                    .put("code", code)
                    .put("expires", expires);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("生成软件代码失败");
    }

    @SysLog("获取软件代码")
    @PostMapping("/list_internal_code")
    public R listInternalCode(@RequestBody PageParam pageParam) {
        try {
            PageUtils pageUtils = this.licenseCodeService.queryPage(pageParam);

            List<LicenseCode> codes = pageUtils.getList();

            return R.ok().put("codes", codes);

        } catch (Exception ex){
            ex.printStackTrace();
        }
        return R.error("获取会员列表失败");
    }

    @SysLog("延期软件代码")
    @PostMapping("/recharge_internal_code")
    public R rechargeInternalCode(@RequestBody Map<String, Object> params) {
        try {
            String code = (String) params.get("code");
            int hours = Integer.parseInt(String.valueOf(params.get("hours")));

            LicenseCode licenseCode = this.licenseCodeService.getCodeDesc(code);
            if (licenseCode == null) {
                return R.error("找不到代码");
            }

            if (licenseCode.getState() == LicenseCodeState.Created.getState()) {
                return R.error("还没激活的代码");
            }

            if (licenseCode.getState() == LicenseCodeState.Deleted.getState()) {
                return R.error("已经删除的代码");
            }

            Date startTime = licenseCode.getServiceStartTime();
            Date endTime = licenseCode.getServiceEndTime();

            if (endTime.getTime() < new Date().getTime()) {
                startTime = new Date();
                endTime = new Date();
            }

            endTime = CommonUtils.addHours(endTime, hours);

            licenseCode.setServiceStartTime(startTime);
            licenseCode.setServiceEndTime(endTime);

            this.licenseCodeService.updateById(licenseCode);

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("延期软件代码");
    }
}
