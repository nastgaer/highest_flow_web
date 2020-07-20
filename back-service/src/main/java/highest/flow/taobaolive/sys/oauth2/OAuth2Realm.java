package highest.flow.taobaolive.sys.oauth2;

import highest.flow.taobaolive.sys.defines.MemberState;
import highest.flow.taobaolive.sys.entity.SysMember;
import highest.flow.taobaolive.sys.entity.SysMemberToken;
import highest.flow.taobaolive.sys.service.MemberService;
import highest.flow.taobaolive.sys.service.ShiroService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OAuth2Realm extends AuthorizingRealm {

    @Autowired
    private ShiroService shiroService;

    @Autowired
    private MemberService memberService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof OAuth2Token;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        SysMember member = (SysMember) principalCollection.getPrimaryPrincipal();

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        // info.setStringPermissions(permsSet);
        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        String token = (String) authenticationToken.getPrincipal();

        SysMemberToken memberToken = shiroService.getMemberTokenByToken(token);
        if (token == null || memberToken == null || memberToken.getExpireTime().getTime() < System.currentTimeMillis()) {
            throw new IncorrectCredentialsException("token失效，请重新登录");
        }

        int memberId = memberToken.getMemberId();
        SysMember member = memberService.getById(memberId);
        if (member == null) {
            throw new DisabledAccountException("账号已被删除,请联系管理员");
        }
        if (member.getState() == MemberState.Normal.getState()) {

        } else if (member.getState() == MemberState.Deleted.getState()) {
            throw new DisabledAccountException("账号已被删除,请联系管理员");
        } else if (member.getState() == MemberState.Suspended.getState()) {
            throw new LockedAccountException("账号已被锁定,请联系管理员");
        }

        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(member, token, getName());
        return info;
    }
}
