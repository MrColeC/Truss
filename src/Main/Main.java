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
	//private static final transient Logger log = LoggerFactory
	//		.getLogger(Main.class);

	/**
	 * TRUSS Main
	 * 
	 */
	public static void main() {
		// Collect any passed arguments
		String loglevel = System.getProperty("loglevel");
		
		// Activate log
		Logging mylog = new Logging(loglevel);
		
		// Start of Execution
		mylog.out("INFO","Truss Launched");

		// Activate Shiro
		Auth subject = new Auth(mylog);

		// Use credentials to authenticate/authorize this instance
		// This is used to determine if this invocation is a:
		// Server - source of work
		// Public/Private calculator - performs work
		// Drop off point - receives completed work
		String[] INPUT = subject.GetCredential();
		Subject currentUser = subject.Login(INPUT[0], INPUT[1]);
		Session session = subject.EstablishSession(currentUser);

		// Set session timeout
		session.setTimeout(86400000);

		// Query session expiration
		long TimeRemaining = session.getTimeout();
		long TimeMin = (TimeRemaining / 1000) / 60;
		long TimeDay = (TimeMin / 1440);
		// If time is better expressed in minutes or days
		if (TimeDay > 0) {
			mylog.out("INFO","The session will time out in " + TimeRemaining + "ms ("
					+ TimeDay + " day(s))");
		} else {
			mylog.out("INFO","The session will time out in " + TimeRemaining + "ms ("
					+ TimeMin + " minutes)");
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
		mylog.out("INFO","Loader Framework terminated");
		System.exit(0);
	}
}
