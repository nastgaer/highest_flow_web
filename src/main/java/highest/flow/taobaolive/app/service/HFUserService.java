package highest.flow.taobaolive.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.app.entity.HFUser;

public interface HFUserService extends IService<HFUser> {

    HFUser register(String username, String password, String machineCode, String mobile, String weixin, int level, int serviceType);

}
