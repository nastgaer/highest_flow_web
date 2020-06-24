package highest.flow.taobaolive.sys.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import highest.flow.taobaolive.sys.dao.MemberTokenDao;
import highest.flow.taobaolive.sys.entity.SysMemberToken;
import highest.flow.taobaolive.sys.service.ShiroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("shiroService")
public class ShiroServiceImpl implements ShiroService {

    @Autowired
    private MemberTokenDao memberTokenDao;

    @Override
    public SysMemberToken getMemberTokenByToken(String token) {
        return memberTokenDao.selectOne(Wrappers.<SysMemberToken>lambdaQuery().eq(SysMemberToken::getToken, token));
    }
}
