package Main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides log levels
 * 
 * @author Cole Christie
 * 
 */
public class Logging {
	private Logger Log;
	private int SetLevel;

	/**
	 * CONSTRCUTOR
	 * 
	 * Pass it the log level to set the verbosity of allowed logging Options are:
	 * INFO, WARN, ERROR, FATAL, OFF Default is FATAL
	 */
	public Logging(String loglevel) {
		// Setup logger via slf4j
		Log = LoggerFactory.getLogger("TRUSS");

		// Send passed value to upper case
		String compare = "FATAL";
		if (!loglevel.isEmpty()) {
			compare = loglevel.toUpperCase();
		}

		// Set log level to passed paramaters (if it can be mapped)
		if (compare.startsWith("INFO")) {
			// All messgaes
			SetLevel = 4;
		} else if (compare.startsWith("WARN")) {
			// Less than info
			SetLevel = 3;
		} else if (compare.startsWith("ERROR")) {
			// Less than warn
			SetLevel = 2;
		} else if (compare.startsWith("OFF")) {
			// No messages
			SetLevel = 0;
		} else {
			// Critcail fauilures only (default)
			SetLevel = 1;
		}

		// This will only show up IF the logging is set to INFO level (4), so
		// it is pointless to make more of these alerts for the other levels
		out("info", "Logging set to INFO");
	}

	/**
	 * Supports multi-level logging with programmatic "silencing". Modes are INFO, WARN and ERROR
	 * 
	 * @param level
	 * @param msg
	 */
	public void out(String level, String msg) {
		// The messages level
		int severity = 0;
		// Send passed value to upper case
		String compare = level.toUpperCase();

		// Set log level to passed parameters (if it can be mapped)
		if (compare.startsWith("INFO")) {
			// All messages
			severity = 4;
		} else if (compare.startsWith("WARN")) {
			// Less than info
			severity = 3;
		} else if (compare.startsWith("ERROR")) {
			// Less than warn
			severity = 2;
		} else {
			// Critical failures only (default)
			severity = 1;
		}

		if (severity <= SetLevel) {
			if (severity == 4) {
				// Log via info utility
				Log.info(msg);
			} else if (severity == 3) {
				// Log via warning utility
				Log.warn(msg);
			} else {
				// Log via error utility
				Log.error(msg);
			}
		}
	}
}
