package highest.flow.taobaolive.taobao.provider;

import highest.flow.taobaolive.common.utils.SpringContextUtils;
import highest.flow.taobaolive.taobao.service.TaobaoApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class TaobaoApiProvider {

    @Value("${taobaolive.simulate:false}")
    private boolean simulate;

    @Bean("taobaoLiveApiService")
    public TaobaoApiService apiService() {
        if (simulate) {
            return (TaobaoApiService) SpringContextUtils.getBean("taobaoApiDemoService");
        }
        return (TaobaoApiService) SpringContextUtils.getBean("taobaoApiService");
    }
}
