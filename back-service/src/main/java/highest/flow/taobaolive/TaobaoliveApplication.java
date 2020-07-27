package highest.flow.taobaolive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TaobaoliveApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaobaoliveApplication.class, new String[] {
        });
    }

}
