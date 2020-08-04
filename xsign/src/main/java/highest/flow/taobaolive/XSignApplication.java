package highest.flow.taobaolive;

import highest.flow.taobaolive.service.MinaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class XSignApplication {

    private static Logger logger = LoggerFactory.getLogger(XSignApplication.class);

    public static void main(String[] args) {
        logger.info("=======================================================");

        SpringApplication.run(XSignApplication.class, args);
    }

}
