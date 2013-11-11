package Main;

import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

/**
 * 
 * @author Cole Christie
 * 
 *         CS 6266 - Masters in Information Security
 * 
 *         Description: TRUSS is a middleware framework that support simple jobs
 *         distribution and collection in a secure manner over insecure
 *         heterogeneous networks. Further, is it intended to be minimalistic in
 *         order to reduce overhead and complexity.
 * 
 */
public class Main {
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
		String Pbind = System.getProperty("bind");
		if (Pbind == null) {
			// Default to binding to port 8080 (as a server or dropoff)
			// This is also checked and overrode in Networking.java
			Pbind = "8080";
		}
		String Psport = System.getProperty("sport");
		if (Psport == null) {
			// Default to connecting to the server on port 8080
			Psport = "8080";
		}
		String Pdport = System.getProperty("dport");
		if (Pdport == null) {
			// Default to connecting to the drop off on port 8080
			Pdport = "8080";
		}
		String Psip = System.getProperty("sip");
		if (Psip == null) {
			// Default to connecting to the server on ip 127.0.0.1 (server is on
			// the same machine as client
			Psip = "127.0.0.1";
		}
		String Pdip = System.getProperty("dip");
		if (Pdip == null) {
			// Default to connecting to the server on ip 127.0.0.1 (server is on
			// the same machine as client
			Pdip = "127.0.0.1";
		}
		//TODO Add fully automated versions of the client

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
		if ((Pusername.isEmpty()) || (Ppass.isEmpty()) || (Pkey.isEmpty())) {
			String[] UserInput = subject.GetCredential();
			currentUser = subject.Login(UserInput[0], UserInput[1]);
		} else {
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
			mylog.out("INFO", "The session will time out in " + TimeRemaining + "ms (" + TimeDay + " day(s))");
		} else {
			mylog.out("INFO", "The session will time out in " + TimeRemaining + "ms (" + TimeMin + " minutes)");
		}

		// Leverage session to launch purpose driven code
		String purpose = (String) session.getAttribute("USE");
		if (purpose == "server") {
			session.setAttribute("workGiven", "0");
			// Launch server (sender)
			Server server = new Server(mylog, subject, Integer.parseInt(Pbind));
			server.start();
			server.LaunchServer();
		} else if (purpose == "dropoff") {
			//TODO Actually implement job drop off
			session.setAttribute("workRecieved", "0");
			// Launch server (receiver)
			Server server = new Server(mylog, subject, Integer.parseInt(Pbind));
			server.start();
			server.LaunchServer();
		} else if (purpose == "private") {
			session.setAttribute("totalJobs", "0");
			session.setAttribute("totalPending", "0");
			session.setAttribute("totalDone", "0");
			// Launch client code (public mode)
			Client client = new Client(mylog, subject, session);
			client.StartClient(Integer.parseInt(Psport), Psip); // Connect to
																// the server
		} else if (purpose == "public") {
			session.setAttribute("totalJobs", "0");
			session.setAttribute("totalPending", "0");
			session.setAttribute("totalDone", "0");
			// Launch client code (private mode)
			Client client = new Client(mylog, subject, session);
			client.StartClient(Integer.parseInt(Psport), Psip); // Connect to
																// the server
		} else {
			// Unknown type or failed authentication
		}

		// User logout
		currentUser.logout();

		// End of Execution
		mylog.out("INFO", "Application terminated");
		System.exit(0);
	}
}
