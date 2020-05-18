package highest.flow.taobaolive.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.app.dao.HFUserDao;
import highest.flow.taobaolive.app.entity.HFUser;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.security.entity.XHeader;
import highest.flow.taobaolive.security.service.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class HFUserController {

    @Autowired
    HFUserDao hfUserDao;

    @Autowired
    private CryptoService cryptoService;

    @PostMapping("/register")
    public R registerUser(@RequestParam(name = "data") String data,
                          @RequestParam(name = "sign") String sign) {

        try {
            boolean verify = cryptoService.verify(data, sign);
            if (!verify) {
                return R.error("参数验证失败");
            }

            String plain = cryptoService.decrypt(data);

            // Json to Object
            ObjectMapper objectMapper = new ObjectMapper();
            HFUser hfUser = objectMapper.readValue(plain, HFUser.class);

            hfUserDao.insertUser(hfUser);

        } catch (Exception ex) {
            return R.error("注册用户失败");
        }

        return R.ok("");
    }

    @PostMapping("/login")
    public R login(@RequestParam(name = "username") String userName,
                   @RequestParam(name = "password") String password,
                   @RequestParam(name = "machineCode") String machineCode,
                   @RequestParam(name = "sign") String sign) {
        return R.ok("");
    }
}
