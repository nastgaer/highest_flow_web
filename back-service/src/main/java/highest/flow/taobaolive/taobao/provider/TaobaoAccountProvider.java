package highest.flow.taobaolive.taobao.provider;

import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class TaobaoAccountProvider {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    private Thread autoUpdate = null;

    public void initialize() {
        try {
            // 该函数经常用，所以打开第一时间获取掉
            taobaoAccountService.getActiveAll();

            autoUpdate = new Thread(new UpdateRunnable());
            autoUpdate.start();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    class UpdateRunnable implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    Date updated = taobaoAccountService.getLastUpdated();

                    // 隔5分钟更新一次
                    int count = taobaoAccountService.reloadUpdatedAccounts(updated);
                    if (count > 0) {
                        logger.info("已经更新" + count + "个小号, 正常小号数：" + taobaoAccountService.getActiveAll().size());
                    }

                    Thread.sleep(5*60 * 1000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
