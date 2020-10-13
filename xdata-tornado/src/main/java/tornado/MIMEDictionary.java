// $Id: MIMEDictionary.java,v 1.10 2001/01/22 23:49:51 nconway Exp $
package tornado;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

public class MIMEDictionary {
	/** A guess about the size of the dictionary. */
	private final static int GUESS_MAPPING_SIZE = 160;
	/** The internal storage of the dictionary of mappings. */
	private final HashMap<String, String> types = new HashMap<String, String>(GUESS_MAPPING_SIZE);

	/**
	 * Constructs a new dictionary, reading the mappings from the specified
	 * <code>File</code>.
	 */
	public MIMEDictionary(File mappings) {
		try {
			final BufferedReader mappingFile = new BufferedReader(new FileReader(mappings));
			String line, type;
			while ((line = mappingFile.readLine()) != null) {
				final StringTokenizer thisLine = new StringTokenizer(line);
				if (thisLine.countTokens() < 2 || line.startsWith("#"))
					continue; // skip comments and lines without tokens

				// the first token in the line is the MIME type
				type = thisLine.nextToken();
				while (thisLine.hasMoreTokens()) {
					// map the next extension to the MIME type
					types.put(thisLine.nextToken(), type);
				}
			}
			mappingFile.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the MIME type of the specified file extension. The file extension
	 * should be passed without a leading period. If no MIME type is found,
	 * "application/octet-stream" is returned.
	 */
	public String getContentType(String extension) {
		final String type = types.get(extension);
		if (type == null) {
			return "application/octet-stream";
		} else {
			return type;
		}
	}

	/**
	 * Returns the MIME type of the specified <code>File</code>. Currently, this
	 * just feeds the extension of the filename to
	 * {@link #getContentType(String)}. However, a future implementation may use
	 * information from <code>File</code> to help determine the MIME type.
	 */
	public String getContentType(File fullFilename) {
		final String filename = fullFilename.getName();
		final int lastPeriod = filename.lastIndexOf('.');
		String extension;
		if (lastPeriod == -1) {
			// if there was no extension, use an empty string
			extension = "";
		} else {
			extension = filename.substring(lastPeriod + 1, filename.length());
		}
		if (lastPeriod == -1) {
			// if there was no extension, use an empty string
			extension = "";
		} else {
			extension = filename.substring(lastPeriod + 1, filename.length());
		}
		return getContentType(extension);
	}

	/**
	 * Adds a mapping to the dictionary. If a mapping for this extension already
	 * exists, it is replaced. This method is synchronized, which should allow
	 * multiple threads to add and query content types concurrently.
	 */
	public synchronized void addContentType(String extension, String type) {
		types.put(extension, type);
	}

}
