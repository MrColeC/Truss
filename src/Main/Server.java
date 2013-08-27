package Main;

import java.net.Socket;

import org.apache.shiro.session.Session;
import org.slf4j.Logger;

/**
 * Provides support for originating work and handing it out as well as receiving
 * completed work
 * 
 * @author Cole Christie
 * 
 */
public class Server {
	private Logger log;
	private Networking network;
	@SuppressWarnings("unused")
	private Session ServerSession;
	private Auth subject;

	/**
	 * CONSTRCUTOR
	 */
	public Server(Logger passedLog, Auth passedSubject) {
		log = passedLog;
		network = new Networking(log, 40000);
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
			new ServerThread(subject, log, socket, passedServer, UIDcounter).start();
		}
	}
}
