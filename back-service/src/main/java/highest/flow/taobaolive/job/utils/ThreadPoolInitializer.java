package highest.flow.taobaolive.job.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class ThreadPoolInitializer {

    @Value("${threadpool.core-pool-size:3000}")
    private int corePoolSize;
    @Value("${threadpool.max-pool-size:6000}")
    private int maxPoolSize;
    @Value("${threadpool.queue-capacity:3000}")
    private int queueCapacity;

    @Bean(name = "rankingExecutor")
    public Executor rankingExecutor() {
        ThreadPoolTaskExecutor threadPoolExecutor = new ThreadPoolTaskExecutor();

        //此方法返回可用处理器的虚拟机的最大数量; 不小于1
//        int core = Runtime.getRuntime().availableProcessors();
//        threadPoolExecutor.setCorePoolSize(core);//设置核心线程数
//        threadPoolExecutor.setMaxPoolSize(core*2 + 1);//设置最大线程数
//        threadPoolExecutor.setKeepAliveSeconds(3);//除核心线程外的线程存活时间
//        threadPoolExecutor.setQueueCapacity(40);//如果传入值大于0，底层队列使用的是LinkedBlockingQueue,否则默认使用SynchronousQueue

        threadPoolExecutor.setCorePoolSize(corePoolSize);
        threadPoolExecutor.setMaxPoolSize(maxPoolSize);
        threadPoolExecutor.setQueueCapacity(queueCapacity);
        threadPoolExecutor.setKeepAliveSeconds(3);
        threadPoolExecutor.setRejectedExecutionHandler(new RejectedExecutionHandlerImpl());
        threadPoolExecutor.setThreadNamePrefix("ranking-");
        return threadPoolExecutor;
    }
}
