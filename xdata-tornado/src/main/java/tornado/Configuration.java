// $Id: Configuration.java,v 1.23 2001/01/25 02:05:59 nconway Exp $
package tornado;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;

import nl.chess.it.util.config.Config;
import nl.chess.it.util.config.ConfigValidationResult;
import nl.chess.it.util.config.ConfigurationException;

public class Configuration extends Config {
	protected Configuration(String resourceName) throws ConfigurationException {
		super(resourceName);
	}

	protected Configuration(Properties resourceName) throws ConfigurationException {
		super(resourceName);
	}

	/**
	 * Name of the file we are looking for to read the configuration.
	 */
	public static final String RESOURCE_NAME = "tornado-config.properties";

	/**
	 * Returns the local ports the server is running on. TODO: Let this return
	 * an array
	 * */
	public final int[] getPorts() {
		final int[] ret = { getInt("ports") };
		return ret;
	}

	/** Returns the root of the HTTP virtual filesystem. */
	public final String getDocumentRoot() {
		return getString("documentRoot");
	}

	/**
	 * Returns the server's signature. This is specified by the HTTP standard,
	 * and consists of the name of the software, and the revision, as well as
	 * any additional components.
	 */
	public final String getVersionSig() {
		return getString("system.versionSignature");
	}

	/**
	 * Returns the version number of the server. This is the plain of the
	 * server, without any additional tags or identifiers.
	 */
	public final String getVersion() {
		return getString("system.version");
	}

	/**
	 * Returns the number of <code>ServerThread</code>s spawned at startup.
	 * Obviously, the number of active threads will change throughout the
	 * lifetime of the server.
	 */
	public final int getStartThreads() {
		return getInt("system.threads.startThreads");
	}

	/**
	 * Returns the maximum number of <code>ServerThread</code>s that will ever
	 * be running concurrently.
	 */
	public final int getMaxThreads() {
		return getInt("system.threads.maxThreads");
	}

	/**
	 * Returns the minimum number of idle threads. If there are fewer idle
	 * threads available than this, new instances of <code>ServerThread</code>
	 * will be spawned by the <code>ThreadManager</code> at a rate of 2 per
	 * second until we reach <code>maxIdleThreads</code>.
	 * 
	 * @see #getMaxIdleThreads()
	 * @see tornado.ThreadManager
	 */
	public final int getMinIdleThreads() {
		return getInt("system.threads.minIdleThreads");
	}

	/**
	 * Returns the maximum number of idle threads. If there are more idle
	 * threads than this, they are killed by the <code>ThreadManager</code>.
	 * 
	 * @see #getMinIdleThreads()
	 * @see tornado.ThreadManager
	 */
	public final int getMaxIdleThreads() {
		return getInt("system.threads.maxIdleThreads");
	}

	/**
	 * Returns the priority of messages that are accepted. Any log messages with
	 * a lower priority than this are ignored.
	 */
	public final int getLogLevel() {
		return getInt("system.logLevel");
	}

	/** Returns the <code>File</code> to use for error logging. */
	public final File getErrorLog() {
		return new File(getString("system.errorLog"));
	}

	/** Returns the <code>File</code> to use for access logging. */
	public final File getAccessLog() {
		return new File(getString("system.accessLog"));
	}

	public final File getMimeTypes() {
		return new File(getConfigurationDir() + File.separator + getString("system.mimeTypes"));
	}

	public final String getConfigurationDir() {
		return getString("configurationDir");
	}

	@SuppressWarnings("unchecked")
	public void validate() {
		final ConfigValidationResult configResult = this.validateConfiguration();

		if (configResult.thereAreErrors()) {
			System.out.println("Errors in configuration");

			for (final Iterator iter = configResult.getErrors().iterator(); iter.hasNext();) {
				System.out.println(" > " + iter.next());
			}

			System.exit(1);
		}

		if (configResult.thereAreUnusedProperties()) {
			System.out.println("Unused properties");

			for (final Iterator iter = configResult.getUnusedProperties().iterator(); iter.hasNext();) {
				System.out.println(" > " + iter.next());
			}
		}

	}
}

/*
 * public class Configuration { private int[] ports = {8080}; private int
 * startThreads = 15; private int maxThreads = 100; private int minIdleThreads =
 * 5; private int maxIdleThreads = 10; private int logLevel = Logger.DEBUG_PRIO;
 * private String documentRoot = "/var/www"; private String version = "0.2.1";
 * private String versionSignature = "Tornado/" + version; private File errorLog
 * = new File("/var/log/tornado/error"); private File accessLog = new
 * File("/var/log/tornado/access"); private File mimeTypes = new
 * File("conf/mime.types"); private boolean configIsValid;
 * 
 * public Configuration() { }
 * 
 * public void saveConfig(File confFile) throws FileNotFoundException{
 * //XMLEncoder decoder = // new XMLEncoder(new BufferedOutputStream(new
 * FileOutputStream(confFile))); //decoder.writeObject( this.toString() );
 * OutputStreamWriter out = new OutputStreamWriter(new BufferedOutputStream(new
 * FileOutputStream(confFile))); try { out.write( this.toString() ); } catch
 * (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); } }
 * 
 * public static Configuration loadConfig(File confFile) throws
 * FileNotFoundException { XMLDecoder decoder = new XMLDecoder(new
 * BufferedInputStream(new FileInputStream(confFile))); return
 * (Configuration)decoder.readObject(); }
 * 
 * 
 * }
 */
