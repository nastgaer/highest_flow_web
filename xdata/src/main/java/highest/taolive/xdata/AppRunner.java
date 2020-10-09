package highest.taolive.xdata;

import highest.taolive.xdata.service.MinaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AppRunner implements CommandLineRunner {

    @Value("${mina.port:8228}")
    private int minaPort = 8228;

    @Override
    public void run(String... args) throws Exception {
        initialize();
    }

    private void initialize() {
        MinaService.start(minaPort);
    }
}
