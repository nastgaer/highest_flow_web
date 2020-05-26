package highest.flow.taobaolive.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.app.entity.HFUser;
import org.springframework.stereotype.Service;

@Service
public interface HFUserService extends IService<HFUser> {

    public HFUser register(String username, String password, String machineCode, String mobile, String weixin, int level, int serviceType);

    public HFUser getUserByUsername(String username);
}
