package Main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides log levels
 * 
 * @author Cole Christie
 * 
 */
public class TLog {
	private Logger Log;
	private int SetLevel;

	/**
	 * CONSTRCUTOR
	 */
	public TLog(String loglevel) {
		// Setup logger via slf4j
		Log = LoggerFactory.getLogger(Main.class);
		
		// Send passed value to upper case
		String compare = loglevel.toUpperCase();
		
		//Set log level to passed paramaters (if it can be mapped)
		if (compare.startsWith("INFO"))
		{
			// All messgaes
			SetLevel = 4;
		}
		else if(compare.startsWith("WARN"))
		{
			// Less than info
			SetLevel = 3;
		}
		else if(compare.startsWith("ERROR"))
		{
			// Less than warn
			SetLevel = 2;
		}
		else if(compare.startsWith("OFF"))
		{
			// No messages
			SetLevel = 0;
		}
		else
		{
			// Critcail fauilures only (default)
			SetLevel = 1;
		}
		
		// This will only show up IF the logging is set to INFO level (4), so
		// it is pointless to make more of these alerts for the other levels 
		out("info","Logging set to INFO")
	}

	/**
	 * Listens for new connections and off loads them to new threads
	 * 
	 * @param passedSession
	 * @param passedServer
	 */
	public void out(String level, String msg) {
		// The messages level
		int severity = 0;
		// Send passed value to upper case
		String compare = level.toUpperCase();

		// Set log level to passed paramaters (if it can be mapped)
		if (compare.startsWith("INFO")) {
			// All messgaes
			severity = 4;
		} else if (compare.startsWith("WARN")) {
			// Less than info
			severity = 3;
		} else if (compare.startsWith("ERROR")) {
			// Less than warn
			severity = 2;
		} else {
			// Critcail fauilures only (default)
			severity = 1;
		}
		
		if (severity <= SetLevel)
		{
			log.info("Truss");
		}
	}
}
