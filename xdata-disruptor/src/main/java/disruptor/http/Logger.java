// $Id: Logger.java,v 1.14 2001/01/25 02:41:24 nconway Exp $
package disruptor.http;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

public class Logger {
	public final static int EMERG_PRIO = 0;
	public final static int ALERT_PRIO = 1;
	public final static int CRIT_PRIO = 2;
	public final static int ERROR_PRIO = 3;
	public final static int WARN_PRIO = 4;
	public final static int NOTICE_PRIO = 5;
	public final static int INFO_PRIO = 6;
	public final static int DEBUG_PRIO = 7;

	private final PrintWriter errorLog;
	private final PrintWriter accessLog;

	/**
	 * Constructs a new <code>Logger</code>. This opens files and prepares to
	 * write logs -- it is the only setup necessary before beginning to log
	 * messages.
	 */
	Logger() {
		final File eLog = HttpConfiguration.getErrorLog();
		final File aLog = HttpConfiguration.getAccessLog();
		final File path = new File(eLog.getParent());
		if (!path.exists())
			path.mkdirs();

		try {
			errorLog = openLog(eLog);
			accessLog = openLog(aLog);
		} catch (final IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	private static PrintWriter openLog(File logFile) throws IOException {
		if (!logFile.exists()) {
			// create the directory tree, create the log file
			logFile.getParentFile().mkdirs();
			logFile.createNewFile();
		}
		// append to existing files, enable autoFlush
		final FileWriter log = new FileWriter(logFile.toString(), true);
		return new PrintWriter(log, true);
	}

	/**
	 * Log a record of the transaction with the client. This should log the
	 * record in the common webserver log format -- thus, we should be
	 * compatible with Apache and others.
	 */
	public void logAccess(CommonLogMessage log) {
		final StringBuffer msg = new StringBuffer();
		msg.append(log.getRemoteHost());
		msg.append(" ");
		msg.append(log.getRemoteLogName());
		msg.append(" ");
		msg.append(log.getUserName());
		msg.append(" [");
		msg.append(log.getDate());
		msg.append("] \"");
		msg.append(log.getRawRequest());
		msg.append("\" ");
		msg.append(log.getStatusCode());
		msg.append(" ");
		msg.append(log.getBytesSent());

		accessLog.println(msg.toString());
	}

	/** Log a message at the specified priority. */
	public void logMessage(String message, int priority) {
		if (HttpConfiguration.getLogLevel() <= priority)
			errorLog.println(new Date().toString() + ": [" + message + "]");
	}

	/**
	 * Log a message at the default priority. This is currently implemented as
	 * logging at <code>NOTICE_PRIO</code>.
	 * 
	 * @see #notice(String)
	 */
	public void logMessage(String message) {
		notice(message);
	}

	/** Log a message at the "EMERG" priority. */
	public void emerg(String message) {
		logMessage(message, EMERG_PRIO);
	}

	/** Log a message at the "ALERT" priority. */
	public void alert(String message) {
		logMessage(message, ALERT_PRIO);
	}

	/** Log a message at the "CRIT" priority. */
	public void crit(String message) {
		logMessage(message, CRIT_PRIO);
	}

	/** Log a message at the "ERROR" priority. */
	public void error(String message) {
		logMessage(message, ERROR_PRIO);
	}

	/** Log a message at the "WARN" priority. */
	public void warn(String message) {
		logMessage(message, WARN_PRIO);
	}

	/** Log a message at the "NOTICE" priority. */
	public void notice(String message) {
		logMessage(message, NOTICE_PRIO);
	}

	/** Log a message at the "INFO" priority. */
	public void info(String message) {
		logMessage(message, INFO_PRIO);
	}

	/** Log a message at the "DEBUG" priority. */
	public void debug(String message) {
		logMessage(message, DEBUG_PRIO);
	}

}
