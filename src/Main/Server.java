package Main;

import java.net.Socket;

/**
 * Provides support for originating work and handing it out as well as receiving
 * completed work
 * 
 * @author Cole Christie
 * 
 */
public class Server extends Thread {
	private Logging mylog;
	private Networking network;
	private Auth subject;
	private int PortUsed;

	/**
	 * CONSTRCUTOR
	 */
	public Server(Logging passedLog, Auth passedSubject, int PortNumber) {
		mylog = passedLog;
		PortUsed = PortNumber;
		network = new Networking(mylog, PortNumber);
		subject = passedSubject;
	}

	/**
	 * Launches the server and provides the UI
	 */
	public void LaunchServer() {
		// Prepare
		String UserInput = null;

		// Display the UI boilerplate
		System.out.println("======================================================================");
		System.out.println("Welcome. This server is accepint connections on port [" + PortUsed + "]");
		System.out.println("Commands are:");
		System.out.println("QUIT - Closes connection with the server and quits");
		System.out.println("* - Anything else is just echo'ed back");
		System.out.println("======================================================================");

		// Enter the UI loop
		while ((UserInput != null) && (UserInput.compareToIgnoreCase("quit") != 0)) {
			System.out.println("You entered:" + UserInput);
			UserInput = "";
		}
		System.exit(0);
	}

	/**
	 * Threads the listening agent so it is seperate from the Servers UI
	 */
	public void run() {
		// Seed client numeric labeling
		int UIDcounter = 0;

		// Setup master thread communication
		Object JobLock = new Object();

		while (1 > 0) {
			// Block until a new connection is made
			Socket socket = network.ListenForNewConnection();
			UIDcounter++;
			new ServerThread(subject, mylog, socket, UIDcounter, JobLock).start();
		}
	}
}
