package highest.flow.taobaolive.app.controller;

import highest.flow.taobaolive.app.entity.HFUser;
import highest.flow.taobaolive.app.service.HFUserService;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.defines.ServiceType;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.security.service.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class HFUserController {

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    HFUserService hfUserService;

    @PostMapping("/info")
    public R info(@RequestParam(name = "data") String data,
                  @RequestParam(name = "sign") String sign) {

        try {
            boolean verify = cryptoService.verify(data, sign);
            if (!verify) {
                return R.error("参数验证失败");
            }

            String plain = cryptoService.decrypt(data);

            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(plain);

            String username = (String)map.get("username");

            HFUser hfUser = hfUserService.getUserByUsername(username);
            if (hfUser == null) {
                return R.error(ErrorCodes.NOT_FOUND_USER, "找不到用户");
            }

            R r = R.ok()
                    .put("state", hfUser.getState())
                    .put("service_start", hfUser.getServiceStartTime())
                    .put("service_end", hfUser.getServiceEndTime());

            if (hfUser.getServiceType() == ServiceType.刷热度.getServiceType()) {
                return r;
            // } else if (hfUser.getServiceType() == ServiceType.高级引流.getServiceType()) {

            } else {
                return R.error("找不到服务类型");
            }

        } catch (Exception ex) {
            return R.error("注册用户失败");
        }
    }

}
