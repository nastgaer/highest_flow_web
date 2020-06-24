package highest.flow.taobaolive.sys.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.sys.dao.MemberTokenDao;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.sys.entity.SysMemberToken;
import highest.flow.taobaolive.sys.service.MemberTokenService;
import highest.flow.taobaolive.sys.oauth2.TokenGenerator;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service("hfUserTokenService")
public class MemberTokenServiceImpl extends ServiceImpl<MemberTokenDao, SysMemberToken> implements MemberTokenService {

    // 12小时后过期
    private final static int EXPIRE = 3600 * 12;

    @Override
    public R createToken(int memberId) {
        // 生成一个token
        String token = TokenGenerator.generateValue();

        // 当前时间
        Date now = new Date();
        // 过期时间
        Date expireTime = new Date(now.getTime() + EXPIRE * 1000);

        SysMemberToken memberToken = baseMapper.selectOne(Wrappers.<SysMemberToken>lambdaQuery().eq(SysMemberToken::getMemberId, memberId));
        if (memberToken == null) {
            memberToken = new SysMemberToken();
            memberToken.setMemberId(memberId);
            memberToken.setToken(token);
            memberToken.setExpireTime(expireTime);
            memberToken.setUpdatedTime(new Date());

            this.save(memberToken);
        } else {
            memberToken.setToken(token);
            memberToken.setExpireTime(expireTime);

            this.updateById(memberToken);
        }

        return R.ok().put("access_token", token).put("expires", expireTime);
    }

    @Override
    public void logout(int memberId) {
        // 生成一个token
        String token = TokenGenerator.generateValue();

        SysMemberToken memberToken = baseMapper.selectOne(Wrappers.<SysMemberToken>lambdaQuery().eq(SysMemberToken::getMemberId, memberId));
        if (memberToken == null) {
            return;
        }

        memberToken.setToken(token);
        this.updateById(memberToken);
    }
}
