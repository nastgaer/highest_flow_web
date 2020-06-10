package highest.flow.taobaolive.app.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import highest.flow.taobaolive.app.dao.HFUserTokenDao;
import highest.flow.taobaolive.app.entity.HFUserToken;
import highest.flow.taobaolive.app.service.ShiroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("shiroService")
public class ShiroServiceImpl implements ShiroService {

    @Autowired
    private HFUserTokenDao hfUserTokenDao;

    @Override
    public HFUserToken getUserTokenByToken(String token) {
        return hfUserTokenDao.selectOne(Wrappers.<HFUserToken>lambdaQuery().eq(HFUserToken::getToken, token));
    }
}