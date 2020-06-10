package highest.flow.taobaolive.app.oauth2;

import highest.flow.taobaolive.app.defines.HFUserState;
import highest.flow.taobaolive.app.entity.HFUser;
import highest.flow.taobaolive.app.entity.HFUserToken;
import highest.flow.taobaolive.app.service.HFUserService;
import highest.flow.taobaolive.app.service.ShiroService;
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
    private HFUserService hfUserService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof OAuth2Token;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        HFUser hfUser = (HFUser) principalCollection.getPrimaryPrincipal();

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        // info.setStringPermissions(permsSet);
        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        String token = (String) authenticationToken.getPrincipal();

        HFUserToken hfUserToken = shiroService.getUserTokenByToken(token);
        if (token == null || hfUserToken.getExpireTime().getTime() < System.currentTimeMillis()) {
            throw new IncorrectCredentialsException("token失效，请重新登录");
        }

        String username = hfUserToken.getUsername();
        HFUser hfUser = hfUserService.getUserByUsername(username);
        if (hfUser == null) {
            throw new DisabledAccountException("账号已被删除,请联系管理员");
        }
        if (hfUser.getState() == HFUserState.Normal.getState()) {

        } else if (hfUser.getState() == HFUserState.Deleted.getState()) {
            throw new DisabledAccountException("账号已被删除,请联系管理员");
        } else if (hfUser.getState() == HFUserState.Suspended.getState()) {
            throw new LockedAccountException("账号已被锁定,请联系管理员");
        }

        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(hfUser, token, getName());
        return info;
    }
}
