// $Id: Tornado.java,v 1.29 2001/01/25 02:06:24 nconway Exp $
package tornado;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import nl.chess.it.util.config.ConfigValidationResult;
import org.slf4j.LoggerFactory;
import tornado.requestHandler.RequestHandlerFactory;

public class Tornado {

    public static org.slf4j.Logger logger = LoggerFactory.getLogger(Tornado.class);

	private ServerPool serverPool;
	private ThreadManager threadManager;

	/** Interface to the config file. */
	private static Configuration config = null;
	/** Looks up the MIME type for a specified file extension. */
	public static MIMEDictionary mime = null;

	/** Mapping of the registered request handlers */
	private static HashMap<String, RequestHandlerFactory> RequestHandler;

	private final OptionSet commandLineOptions;

	/**
	 * Constructs a server with the specified options. The server is prepared
	 * for production state, but it is fully started: we start
	 * <code>ServerThread</code>s, but don't bind to a local port. It is passed
	 * the command-line arguments specified, and it processes these.
	 * 
	 * @see #start()
	 */
	public Tornado(OptionSet commandLineOptions) {
		this.commandLineOptions = commandLineOptions;
		RequestHandler = new HashMap<String, RequestHandlerFactory>();
	}

	public boolean execute() {
		String confDir = "conf";
		if (commandLineOptions.hasArgument("c"))
			confDir = (String) commandLineOptions.valueOf("c");

		if (Tornado.getConfig(confDir) != null) {
			serverPool = new ServerPool(config.getStartThreads());
			threadManager = new ThreadManager(serverPool);
			mime = new MIMEDictionary(config.getMimeTypes());

			final int[] ports = Tornado.config.getPorts();
			for (int i = 0; i < ports.length; ++i) {
				final Thread t = new ListenThread(serverPool, ports[i]);
				t.start();
			}
			System.out.println("Tornado is ready to accept connections");
			threadManager.run();

			return true;

		} else {
			logger.error("Unable to load configuration");
			return false;
		}
	}

	/**
	 * Registers a request handler which implemets the
	 * <code>RequestHandlerInterface</code> The pattern is used to determin
	 * which request will be handled by this handler.
	 */
	public static void registerRequestHandler(String pattern, RequestHandlerFactory interf) {
		Tornado.getRequestHandler().put(pattern, interf);
	}

	public static Configuration getConfig() {
		return getConfig(".");
	}

	public static Configuration getConfig(final String path) {
		if (config == null) {
			final String file = path + File.separator + Configuration.RESOURCE_NAME;
			final Properties prop = new Properties();
			try {
				prop.load(new FileInputStream(file));

				prop.setProperty("configurationDir", path);
				config = new Configuration(prop);
				final ConfigValidationResult configResult = config.validateConfiguration();
				if (configResult.thereAreErrors()) {
					// display errors here
					System.out.println("Errors in configuration");

					for (final Iterator iter = configResult.getErrors().iterator(); iter.hasNext();) {
						System.out.println(" > " + iter.next());
					}

					System.exit(1);
				}

				prop.store(new FileOutputStream(file), "");
			} catch (final FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return config;
	}

	public static HashMap<String, RequestHandlerFactory> getRequestHandler() {
		return RequestHandler;
	}
}
