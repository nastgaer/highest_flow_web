package highest.flow.taobaolive;

import highest.flow.taobaolive.service.MinaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AppRunner implements CommandLineRunner {

    private static Logger logger = LoggerFactory.getLogger(AppRunner.class);

    @Override
    public void run(String... args) throws Exception {
        initialize();
    }

    private void initialize() {
        logger.info(">>>>>>>>>>> AppRunner >>>>>>>>>>>>>>>>");

        MinaService.start();
    }
}
