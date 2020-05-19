package highest.flow.taobaolive.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.app.entity.HFUserToken;
import highest.flow.taobaolive.common.utils.R;
import org.springframework.stereotype.Service;

@Service
public interface HFUserTokenService extends IService<HFUserToken> {

    public R createToken(String username);

    public void logout(String username);

}
