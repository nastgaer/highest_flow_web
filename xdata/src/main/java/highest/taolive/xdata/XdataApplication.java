package highest.taolive.xdata;

import highest.taolive.xdata.service.MinaService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class XdataApplication {

    public static void main(String[] args) {
        Log4jConfiguration.configure("xsign.log");

        SpringApplication.run(XdataApplication.class, args);

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                super.run();

                MinaService.stop();
            }
        });
    }

}
