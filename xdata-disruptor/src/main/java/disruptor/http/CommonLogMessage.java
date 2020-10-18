// $Id: CommonLogMessage.java,v 1.10 2001/01/19 04:53:21 nconway Exp $
package disruptor.http;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonLogMessage {
	private static final SimpleDateFormat logDateMaker = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss");

	private String remoteHost = "-";
	private String remoteLogName = "-";
	private String userName = "-";
	private String date = formatDate(new Date());
	private String rawRequest = "-";
	private int statusCode = 0;
	private long dataSent = 0;

	/** Creates a new instance with the specified field values. */
	CommonLogMessage(String s1, String s2, String s3, String s4, String s5, int i1, long l1) {
		remoteHost = s1;
		remoteLogName = s2;
		userName = s3;
		date = s4;
		rawRequest = s5;
		statusCode = i1;
		dataSent = l1;
	}

	/**
	 * Creates a new instance, reading information from the specified
	 * <code>Response</code>.
	 */
	CommonLogMessage(Request request) {
		remoteHost = request.getHostAddress();
		rawRequest = request.getRawRequest();
	}

	/**
	 * Returns the specified <code>Date</code>, formatted according to the
	 * Common Web Server Log Format.
	 */
	private static String formatDate(Date date) {
		return logDateMaker.format(date).toString();
	}

	/**
	 * Returns simple representation of the data in this object. Not yet
	 * implemented.
	 */
	@Override
	public String toString() {
		return null;
	}

	/** Returns the IP address of the HTTP client. */
	public String getRemoteHost() {
		return remoteHost;
	}

	/**
	 * Returns the RFC1413 (ident) username of the HTTP client. Currently, no
	 * RFC1413-related methods are implemented, so it will almost always return
	 * the default "-".
	 */
	public String getRemoteLogName() {
		return remoteLogName;
	}

	/**
	 * Returns the HTTP Authentication username of the HTTP client. Currently,
	 * no HTTP Authentication-related methods are implemented, so this will
	 * almost always return the default "-".
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Returns the date the connection with the client was begun. Note that this
	 * does <b>not</b> return a <code>Date</code> object, and thus is not
	 * convenient to modify.
	 */
	public String getDate() {
		return date;
	}

	/** Returns the raw HTTP request sent by the client. */
	public String getRawRequest() {
		return rawRequest;
	}

	/**
	 * Returns the HTTP status code of the completed transaction with the
	 * client.
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * Returns the number of bytes sent to the client during this HTTP
	 * transaction. Currently not implemented. Keep in mind this need to be a
	 * "long" integer to return huge filesizes.
	 */
	public long getBytesSent() {
		return dataSent;
	}

	/** Sets the remote hostname or IP address. */
	public void setRemoteHost(String host) {
		this.remoteHost = host;
	}

	/** Sets the remote RFC1413 (ident) username. */
	public void setRemoteLogName(String userName) {
		this.remoteLogName = userName;
	}

	/** Sets the HTTP Authentication username. */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Sets the date of the transaction. Note: this should be passed a
	 * <code>Date</code> object. It will be converted to the correct format and
	 * stored as a <code>String</code>.
	 */
	public void setDate(Date date) {
		this.date = formatDate(date);
	}

	/** Sets the raw HTTP request line. */
	public void setRawRequest(String request) {
		this.rawRequest = request;
	}

	/** Sets the HTTP status code. */
	public void setStatusCode(int code) {
		this.statusCode = code;
	}

	/** Sets the number of bytes sent in this transaction. */
	public void setBytesSent(long length) {
		this.dataSent = length;
	}

}
