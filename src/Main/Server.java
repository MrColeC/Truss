package Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
		System.out.println("Welcome. This server is accepting connections on port [" + PortUsed + "]");
		System.out.println("Commands are:");
		System.out.println("QUIT - Closes connection with the server and quits");
		System.out.println("* - Anything else is just echo'ed back");
		System.out.println("======================================================================");

		// Enter the UI loop
		UserInput = readUI();
		while ((UserInput != null) && (UserInput.compareToIgnoreCase("quit") != 0)) {
			System.out.println("You entered:" + UserInput);
			UserInput = readUI();
		}
		
	}

	/**
	 * Threads the listening agent so it is separate from the Servers UI
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
	
	/**
	 * Reads input provided by the user, returns a string
	 * 
	 * @return
	 */
	private String readUI() {
		System.out.flush();
		System.out.print("> ");
		System.out.flush();
		String data = null;
		BufferedReader inputHandle = new BufferedReader(new InputStreamReader(System.in));
		boolean wait = true;
		while (wait) {
			try {
				if (inputHandle.ready()) {
					wait = false;
				} else {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						mylog.out("ERROR", "Failed to sleep");
					}
				}
			} catch (IOException err) {
				mylog.out("ERROR", "Failed to check if buffered input was ready [" + err + "]");
			}
		}
		try {
			data = inputHandle.readLine();
		} catch (IOException err) {
			mylog.out("ERROR", "Failed to collect user input [" + err + "]");
		}
		return data;
	}
}
