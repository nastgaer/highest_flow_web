package highest.flow.taobaolive.sys.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.open.sys.*;
import highest.flow.taobaolive.sys.dao.MemberDao;
import highest.flow.taobaolive.sys.entity.SysMember;
import highest.flow.taobaolive.sys.service.MemberService;
import highest.flow.taobaolive.sys.service.MemberTokenService;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sys")
public class UserController {

    @Autowired
    private MemberService memberService;

    @PostMapping("/register")
    public R register(@RequestBody RegisterUserParam registerUserParam) {
        try {
            SysMember member = memberService.register(registerUserParam.getUsername(),
                    registerUserParam.getPassword(),
                    registerUserParam.getMobile(),
                    registerUserParam.getComment(),
                    registerUserParam.getRole(),
                    registerUserParam.getState());

            if (member == null) {
                return R.error("注册用户失败");
            }

            return R.ok().put(member.getId());

        } catch (Exception ex){
            return R.error("注册用户失败");
        }
    }

    @PostMapping("/list")
    public R list(@RequestBody PageParam pageParam) {
        try {
            List<SysMember> members = this.memberService.list();

            return R.ok().put("users", members).put("total_count", members.size());

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
