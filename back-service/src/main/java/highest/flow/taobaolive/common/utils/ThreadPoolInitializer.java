package highest.flow.taobaolive.common.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class ThreadPoolInitializer {

    @Bean(name = "rankingThreadPool")
    public Executor rankingExecutor() {
        ThreadPoolTaskExecutor threadPoolExecutor = new ThreadPoolTaskExecutor();
        threadPoolExecutor.setCorePoolSize(1);
        threadPoolExecutor.setMaxPoolSize(3000);
        threadPoolExecutor.setQueueCapacity(1000);
        threadPoolExecutor.setThreadNamePrefix("ranking-");
        return threadPoolExecutor;
    }
}
