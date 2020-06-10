package highest.flow.taobaolive.app.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.app.dao.HFUserTokenDao;
import highest.flow.taobaolive.app.entity.HFUserToken;
import highest.flow.taobaolive.app.oauth2.TokenGenerator;
import highest.flow.taobaolive.app.service.HFUserTokenService;
import highest.flow.taobaolive.common.utils.R;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service("hfUserTokenService")
public class HFUserTokenServiceImpl extends ServiceImpl<HFUserTokenDao, HFUserToken> implements HFUserTokenService {

    // 12小时后过期
    private final static int EXPIRE = 3600 * 12;

    @Override
    public R createToken(String username) {
        // 生成一个token
        String token = TokenGenerator.generateValue();

        // 当前时间
        Date now = new Date();
        // 过期时间
        Date expireTime = new Date(now.getTime() + EXPIRE * 1000);

        HFUserToken userToken = baseMapper.selectOne(Wrappers.<HFUserToken>lambdaQuery().eq(HFUserToken::getUsername, username));
        if (userToken == null) {
            userToken = new HFUserToken();
            userToken.setUsername(username);
            userToken.setToken(token);
            userToken.setExpireTime(expireTime);
            userToken.setUpdatedTime(new Date());

            this.save(userToken);
        } else {
            userToken.setToken(token);
            userToken.setExpireTime(expireTime);

            this.updateById(userToken);
        }

        return R.ok().put("token", token).put("expire", expireTime);
    }

    @Override
    public void logout(String username) {
        // 生成一个token
        String token = TokenGenerator.generateValue();

        HFUserToken userToken = baseMapper.selectOne(Wrappers.<HFUserToken>lambdaQuery().eq(HFUserToken::getUsername, username));
        if (userToken == null) {
            return;
        }

        userToken.setToken(token);
        this.updateById(userToken);
    }
}
