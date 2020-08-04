package highest.taolive.xdata;

import highest.taolive.xdata.service.MinaService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AppRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        initialize();
    }

    private void initialize() {
        MinaService.start();
    }
}
