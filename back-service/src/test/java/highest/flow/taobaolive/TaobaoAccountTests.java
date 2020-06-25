package highest.flow.taobaolive;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountState;
import highest.flow.taobaolive.taobao.entity.LiveRoom;
import highest.flow.taobaolive.taobao.entity.Product;
import highest.flow.taobaolive.taobao.entity.TaobaoAccount;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import highest.flow.taobaolive.taobao.service.TaobaoApiService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(classes = TaobaoliveApplication.class, args = "test")
public class TaobaoAccountTests {

    @Autowired
    private TaobaoApiService taobaoApiService;

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    void getNewDeviceId() {
        List<TaobaoAccount> taobaoAccounts = taobaoAccountService.list();
        for (TaobaoAccount taobaoAccount : taobaoAccounts) {
            if (taobaoAccount.getState() == TaobaoAccountState.Normal.getState()) {
                R r = taobaoApiService.getH5Token(taobaoAccount);
                if (r.getCode() == ErrorCodes.SUCCESS) {
                    System.out.println(taobaoAccount.getNick() + ": H5Token成功");
                } else {
                    System.out.println(taobaoAccount.getNick() + ": H5Token失败，" + r.getMsg());
                }

                r = taobaoApiService.getUmtidToken();
                if (r.getCode() == ErrorCodes.SUCCESS) {
                    System.out.println(taobaoAccount.getNick() + ": getUmtidToken成功，" + r.get("umtid").toString());
                } else {
                    System.out.println(taobaoAccount.getNick() + ": getUmtidToken失败，" + r.getMsg());
                }

                r = taobaoApiService.getNewDeviceId(taobaoAccount);
                if (r.getCode() == ErrorCodes.SUCCESS) {
                    System.out.println(taobaoAccount.getNick() + ": 获取机器码成功，" + r.get("device_id"));
                } else {
                    System.out.println(taobaoAccount.getNick() + ": 获取机器码失败，" + r.getMsg());
                }
            }
        }
    }
}
