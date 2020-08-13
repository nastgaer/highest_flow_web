package highest.taolive.xdata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class XdataApplication {

    public static void main(String[] args) {
        Log4jConfiguration.configure("xsign.log");

        SpringApplication.run(XdataApplication.class, args);
    }

}
