package highest.flow.taobaolive.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.sys.entity.SysMemberToken;
import org.springframework.stereotype.Service;

@Service
public interface MemberTokenService extends IService<SysMemberToken> {

    public R createToken(int memberId);

    public void logout(int memberId);

}
