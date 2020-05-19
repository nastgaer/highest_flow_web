package highest.flow.taobaolive.app.service.impl;

import org.apache.commons.lang.RandomStringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.app.dao.HFUserDao;
import highest.flow.taobaolive.app.entity.HFUser;
import highest.flow.taobaolive.app.service.HFUserService;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service("hfUserService")
public class HFUserServiceImpl extends ServiceImpl<HFUserDao, HFUser> implements HFUserService {

    @Override
    public HFUser register(String username, String password, String machineCode, String mobile, String weixin, int level, int serviceType) {
        HFUser hfUser = new HFUser();

        hfUser.setUsername(username);
        hfUser.setPassword(password);
        hfUser.setMachineCode(machineCode);
        hfUser.setMobile(mobile);
        hfUser.setWeixin(weixin);
        hfUser.setLevel(level);
        hfUser.setServiceType(serviceType);
        hfUser.setCreatedTime(new Date());
        hfUser.setUpdatedTime(new Date());

        // sha256加密
        String salt = RandomStringUtils.randomAlphanumeric(20);
        hfUser.setPassword(new Sha256Hash(hfUser.getPassword(), salt).toHex());
        hfUser.setSalt(salt);

        this.save(hfUser);

        return null;
    }
}
