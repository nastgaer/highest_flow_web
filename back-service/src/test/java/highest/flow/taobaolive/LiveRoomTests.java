package highest.flow.taobaolive;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountState;
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
public class LiveRoomTests {

    @Autowired
    private TaobaoApiService taobaoApiService;

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    void getProductItem() {
        try {
            TaobaoAccount activeAccount = null;
            List<TaobaoAccount> taobaoAccounts = taobaoAccountService.list();
            for (TaobaoAccount taobaoAccount : taobaoAccounts) {
                if (taobaoAccount.getState() == TaobaoAccountState.Normal.getState()) {
                    activeAccount = taobaoAccount;
                    break;
                }
            }

            if (activeAccount != null) {
                taobaoApiService.getH5Token(activeAccount);

                R r = taobaoApiService.getProductItemInfo(activeAccount, "618917018752");

                ObjectMapper objectMapper = new ObjectMapper();
                System.out.println(objectMapper.writeValueAsString(r));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void openProduct() {
        try {
            TaobaoAccount activeAccount = null;
            List<TaobaoAccount> taobaoAccounts = taobaoAccountService.list();
            for (TaobaoAccount taobaoAccount : taobaoAccounts) {
                if (taobaoAccount.getState() == TaobaoAccountState.Normal.getState()) {
                    activeAccount = taobaoAccount;
                    System.out.println("选择用户：" + activeAccount.getNick());
                    break;
                }
            }

            if (activeAccount != null) {
                taobaoApiService.getH5Token(activeAccount);

                ObjectMapper objectMapper = new ObjectMapper();

                R r = taobaoApiService.getProductItemInfo(activeAccount, "610704107634");
                System.out.println(objectMapper.writeValueAsString(r));

                r = taobaoApiService.openProduct(activeAccount, "610704107634");
                System.out.println(objectMapper.writeValueAsString(r));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
