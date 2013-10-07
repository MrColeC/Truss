package Main;

import java.net.Socket;
import org.apache.shiro.session.Session;

/**
 * Provides support for originating work and handing it out as well as receiving
 * completed work
 * 
 * @author Cole Christie
 * 
 */
public class Server {
	private Logging mylog;
	private Networking network;
	@SuppressWarnings("unused")
	private Session ServerSession;
	private Auth subject;

	/**
	 * CONSTRCUTOR
	 */
	public Server(Logging passedLog, Auth passedSubject, int PortNumber) {
		mylog = passedLog;
		network = new Networking(mylog, PortNumber);
		subject = passedSubject;
	}

	/**
	 * Listens for new connections and off loads them to new threads
	 * 
	 * @param passedSession
	 * @param passedServer
	 */
	public void LaunchServer(Session passedSession, Server passedServer) {
		ServerSession = passedSession;
		int UIDcounter = 0;
		while (1 > 0) {
			// Get a new connection
			Socket socket = network.ListenForNewConnection();
			UIDcounter++;
			new ServerThread(subject, mylog, socket, passedServer, UIDcounter).start();
		}
	}
}
