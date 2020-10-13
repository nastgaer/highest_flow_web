import mina.service.MinaService;
import tornado.Tornado;

import java.net.URL;
import java.net.URLDecoder;

public class Application {

    /**
     * Bootup the server from the console interface. This is very simple - it
     * just creates a new instance of <code>Tornado</code> and starts it.
     *
     * @see #Tornado(String[])
     */
    public static void main(String[] args) {
        int defaultPort = 7227;
        int minaPort = defaultPort;

//        if (args.length > 1) {
//            try {
//                minaPort = Integer.parseInt(args[0]);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                minaPort = defaultPort;
//            }
//        }

        MinaService.start(minaPort);

//        URL resourcePath = Tornado.class.getClassLoader().getResource("conf/");
//        String filePath = URLDecoder.decode(resourcePath.getPath());
//        if (filePath.startsWith("/")) {
//            filePath = filePath.substring(1).trim();
//        }

//        final Tornado server = new Tornado(new String[] {"-c" + filePath});

        final Tornado server = new Tornado(args);
        if (!server.execute()) {
            MinaService.stop();
        }
    }
}
