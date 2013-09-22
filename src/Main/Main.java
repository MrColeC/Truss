package Main;

import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

/**
 * 
 * @author Cole Christie CS 6266 - Masters in Information Security - Capstone
 *         Project
 * 
 *         Description: TRUSS is a middleware framework that support simple jobs
 *         distribtuion and collection in a secure manner over unsecure
 *         heterogenous networks. Further, is it intended to be minimalistic in
 *         order to reduce overhead and complexitiy.
 * 
 */
public class Main {

	/**
	 * Creates simple logging framework
	 */
	// private static final transient Logger log = LoggerFactory
	// .getLogger(Main.class);

	/**
	 * TRUSS Main
	 * 
	 */
	public static void main(String[] args) {
		// Collect any passed arguments
		String Ploglevel = System.getProperty("loglevel");
		if (Ploglevel == null) {
			// Default to FATAL log level
			Ploglevel = "FATAL";
		}
		String Pusername = System.getProperty("user");
		if (Pusername == null) {
			// Default to empty string - force interactive user prompt
			Pusername = "";
		}
		String Ppass = System.getProperty("pass");
		if (Ppass == null) {
			// Default to empty string - force interactive user prompt
			Ppass = "";
		}
		String Pkey = System.getProperty("key");
		if (Pkey == null) {
			// Default to empty string - force interactive user prompt
			Pkey = "";
		}

		// Activate log
		Logging mylog = new Logging(Ploglevel);

		// Start of Execution
		mylog.out("INFO", "Truss Launched");

		// Activate Shiro
		Auth subject = new Auth(mylog);

		// Use credentials to authenticate/authorize this instance
		// This is used to determine if this invocation is a:
		// Server - source of work
		// Public/Private calculator - performs work
		// Drop off point - receives completed work
		Subject currentUser;
		if ((Pusername.isEmpty()) || (Ppass.isEmpty()) || (Pkey.isEmpty()))
		{
			String[] UserInput = subject.GetCredential();
			currentUser = subject.Login(UserInput[0], UserInput[1]);
		}
		else
		{
			subject.SetPSK(Pkey);
			currentUser = subject.Login(Pusername, Ppass);
		}
		Session session = subject.EstablishSession(currentUser);

		// Set session timeout
		session.setTimeout(86400000);

		// Query session expiration
		long TimeRemaining = session.getTimeout();
		long TimeMin = (TimeRemaining / 1000) / 60;
		long TimeDay = (TimeMin / 1440);
		// If time is better expressed in minutes or days
		if (TimeDay > 0) {
			mylog.out("INFO", "The session will time out in " + TimeRemaining
					+ "ms (" + TimeDay + " day(s))");
		} else {
			mylog.out("INFO", "The session will time out in " + TimeRemaining
					+ "ms (" + TimeMin + " minutes)");
		}

		// Leverage session to launch purpose driven code
		String purpose = (String) session.getAttribute("USE");
		if (purpose == "server") {
			session.setAttribute("workGiven", "0");
			// Launch server (sender)
			Server server = new Server(mylog, subject);
			server.LaunchServer(session, server);
		} else if (purpose == "dropoff") {
			session.setAttribute("workRecieved", "0");
			// Launch server (receiver)
			Server server = new Server(mylog, subject);
			server.LaunchServer(session, server);
		} else if (purpose == "private") {
			session.setAttribute("totalJobs", "0");
			session.setAttribute("totalPending", "0");
			session.setAttribute("totalDone", "0");
			// Launch client code (public mode)
			Client client = new Client(mylog, subject);
			client.StartClient(40000, "127.0.0.1");
		} else if (purpose == "public") {
			session.setAttribute("totalJobs", "0");
			session.setAttribute("totalPending", "0");
			session.setAttribute("totalDone", "0");
			// Launch client code (private mode)
			Client client = new Client(mylog, subject);
			client.StartClient(40000, "127.0.0.1");
		} else {
			// Unknown type or failed authentication
		}

		// User logout
		currentUser.logout();

		// End of Execution
		mylog.out("INFO", "Loader Framework terminated");
		System.exit(0);
	}
}
