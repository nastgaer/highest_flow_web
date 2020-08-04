package highest.flow.taobaolive;

import highest.flow.taobaolive.service.MinaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class XSignApplication {

    public static void main(String[] args) {
        SpringApplication.run(XSignApplication.class, args);
        MinaService.start();
    }

}
