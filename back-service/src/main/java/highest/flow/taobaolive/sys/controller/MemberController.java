package highest.flow.taobaolive.sys.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.utils.CommonUtils;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.open.sys.*;
import highest.flow.taobaolive.sys.dao.MemberDao;
import highest.flow.taobaolive.sys.entity.SysMember;
import highest.flow.taobaolive.sys.service.MemberRoleGroupService;
import highest.flow.taobaolive.sys.service.MemberRoleService;
import highest.flow.taobaolive.sys.service.MemberService;
import highest.flow.taobaolive.sys.service.MemberTokenService;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sys")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @PostMapping("/register")
    public R register(@RequestBody RegisterUserParam registerUserParam) {
        try {
            SysMember member = memberService.register(registerUserParam.getMemberName(),
                    registerUserParam.getPassword(),
                    registerUserParam.getMobile(),
                    registerUserParam.getComment(),
                    registerUserParam.getRole(),
                    registerUserParam.getState());

            if (member == null) {
                return R.error("注册用户失败");
            }

            return R.ok().put("member_id", member.getId());

        } catch (Exception ex){
            return R.error("注册用户失败");
        }
    }

    @PostMapping("/list")
    public R list(@RequestBody PageParam pageParam) {
        try {
            List<SysMember> members = this.memberService.list();

            List<Map<String, Object>> memberList = new ArrayList<>();
            for (SysMember sysMember : members) {
                List<String> roles = memberService.getRoles(sysMember);

                Map<String, Object> memberMap = new HashMap<>();
                memberMap.put("id", sysMember.getId());
                memberMap.put("member_name", sysMember.getMemberName());
                memberMap.put("role", roles);
                memberMap.put("mobile", sysMember.getMobile());
                memberMap.put("comment", sysMember.getComment());
                memberMap.put("state", sysMember.getState());
                memberMap.put("created_time", CommonUtils.dateToTimestamp(sysMember.getCreatedTime()));
                memberMap.put("updated_time", CommonUtils.dateToTimestamp(sysMember.getUpdatedTime()));

                memberList.add(memberMap);
            }

            return R.ok().put("users", memberList).put("total_count", memberList.size());

        } catch (Exception ex){
            return R.error("注册用户失败");
        }
    }

    @PostMapping("/update")
    public R update(@RequestBody SysMember sysMember) {
        try {
            if (this.memberService.update(sysMember, Wrappers.<SysMember>lambdaQuery().eq(SysMember::getId, sysMember.getId()))) {
                return R.ok();
            }

            return R.error("注册用户失败");

        } catch (Exception ex){
            return R.error("注册用户失败");
        }
    }
}
