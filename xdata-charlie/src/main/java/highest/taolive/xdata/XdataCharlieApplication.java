package highest.taolive.xdata;

import highest.taolive.xdata.http.RequestContext;
import highest.taolive.xdata.http.SimpleWebServer;
import highest.taolive.xdata.service.MinaService;

public class XdataCharlieApplication {

    public static void main(String[] args) {
        Log4jConfiguration.configure("xsign.log");

        String host = "127.0.0.1";
        int httpPort = 9999, minaPort = 8228;

        for (String arg : args) {
            String[] words = arg.split("=");
            if (words[0].compareToIgnoreCase("--http.host") == 0) {
                host = words[1];
            } else if (words[0].compareToIgnoreCase("--http.port") == 0) {
                httpPort = Integer.parseInt(words[1]);
            } else if (words[0].compareToIgnoreCase("--mina.port") == 0) {
                minaPort = Integer.parseInt(words[1]);
            }
        }

        try {
            RequestContext requestContext = new RequestContext();
            requestContext.setHost(host);
            requestContext.setPort(httpPort);
            requestContext.setCorePoolSize(200);
            requestContext.setMaxPoolSize(500);

            MinaService.start(minaPort);

            SimpleWebServer simpleWebServer = new SimpleWebServer(requestContext);
            simpleWebServer.start();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                super.run();

                MinaService.stop();
            }
        });
    }

}
