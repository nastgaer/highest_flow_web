package highest.flow.taobaolive.sys.controller;

import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.open.sys.LoginParam;
import highest.flow.taobaolive.open.sys.LogoutParam;
import highest.flow.taobaolive.sys.entity.SysMember;
import highest.flow.taobaolive.sys.service.MemberService;
import highest.flow.taobaolive.sys.service.MemberTokenService;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys")
public class LoginController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberTokenService memberTokenService;

    @PostMapping("/login")
    public R login(@RequestBody LoginParam loginParam) {
        try {
            SysMember sysMember = memberService.getMemberByUsername(loginParam.getUsername());
            if (sysMember == null) {
                return R.error(ErrorCodes.NOT_FOUND_USER, "找不到用户");
            }

            if (!sysMember.getPassword().equals(new Sha256Hash(loginParam.getPassword(), sysMember.getSalt()).toHex())) {
                return R.error(ErrorCodes.INVALID_PASSWORD, "账号或密码不正确");
            }

            R r = memberTokenService.createToken(username);


            return r;

        } catch (Exception ex){
            return R.error("登录用户失败");
        }
    }

    @PostMapping("/logout")
    public R logout(@RequestBody LogoutParam logoutParam) {
        try {
            SysMember sysMember = memberService.getMemberByUsername(loginParam.getUsername());
            if (sysMember == null) {
                return R.error(ErrorCodes.NOT_FOUND_USER, "找不到用户");
            }

            memberTokenService.logout(sysMember.getUsername());

            return R.ok();

        } catch (Exception ex){
            return R.error("注销用户失败");
        }
    }
}
