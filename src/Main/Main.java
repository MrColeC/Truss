package Main;

import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Cole Christie
 * 		   CS 6262 - Network Security Class Project First Version of Loader
 *         Framework for the RCSB PDB @ UCSD
 * 
 */
public class Main {

	/**
	 * Creates simple logging framework
	 */
	private static final transient Logger log = LoggerFactory
			.getLogger(Main.class);

	/**
	 * Main of Loader Framework
	 * Authenticates run time purpose and diverts invocations to desired code base
	 * @param args
	 */
	public static void main(String[] args) {
		// Start of Execution
		log.info("Truss");

		// Activate Logger (SLF4J)
		log.info("Logging enabled");

		// Activate Shiro
		Auth subject = new Auth(log);

		// Use credentials to authenticate/authorize this instance
		// This is used to determine if this invocation is a:
		// Server - source of work
		// Public/Private calculator - performs work
		// Drop off point - receives completed work
		String[] INPUT = subject.GetCredential();
		Subject currentUser = subject.Login(INPUT[0], INPUT[1]);
		Session session = subject.EstablishSession(currentUser);

		//Set session timeout
		session.setTimeout(86400000);
		
		// Query session expiration
		long TimeRemaining = session.getTimeout();
		long TimeMin = (TimeRemaining / 1000) / 60;
		long TimeDay = (TimeMin / 1440);
		//If time is better expressed in minutes or days
		if (TimeDay > 0)
		{
			log.info("The session will time out in " + TimeRemaining + "ms ("
					+ TimeDay + " day(s))");
		}
		else
		{
			log.info("The session will time out in " + TimeRemaining + "ms ("
					+ TimeMin + " minutes)");	
		}

		// Leverage session to launch purpose driven code
		String purpose = (String) session.getAttribute("USE");
		if (purpose == "server") {
			session.setAttribute("workGiven", "0");
			// Launch server (sender)
			Server server = new Server(log, subject);
			server.LaunchServer(session, server);
		} else if (purpose == "dropoff") {
			session.setAttribute("workRecieved", "0");
			// Launch server (receiver)
			Server server = new Server(log, subject);
			server.LaunchServer(session, server);
		} else if (purpose == "private") {
			session.setAttribute("totalJobs", "0");
			session.setAttribute("totalPending", "0");
			session.setAttribute("totalDone", "0");
			// Launch client code (public mode)
			Client client = new Client(log, subject);
			client.StartClient(40000, "127.0.0.1");
		} else if (purpose == "public") {
			session.setAttribute("totalJobs", "0");
			session.setAttribute("totalPending", "0");
			session.setAttribute("totalDone", "0");
			// Launch client code (private mode)
			Client client = new Client(log, subject);
			client.StartClient(40000, "127.0.0.1");
		} else {
			// Unknown type or failed authentication
		}

		// User logout
		currentUser.logout();

		// End of Execution
		log.info("Loader Framework terminated");
		System.exit(0);
	}
}
