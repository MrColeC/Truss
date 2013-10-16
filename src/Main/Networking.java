package Main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

/**
 * Provides networking support to securely bind to a port to listen or connect
 * to a remote port for communication
 * 
 * @author Cole Christie
 * 
 */
public class Networking {
	private Logging mylog;
	private ServerSocket serverSocket;
	private Socket ClientSocket;
	private DataInputStream receive;
	private DataOutputStream send;

	// If verbose byte level network logging should be displayed
	private static boolean VERBOSE = false;

	/**
	 * CONSTRUCTOR Basic version
	 */
	public Networking(Logging passedLog) {
		mylog = passedLog;
	}

	/**
	 * CONSTRUCTOR Server version
	 */
	public Networking(Logging passedLog, int port) {
		mylog = passedLog;
		BindServer(port);
	}

	/**
	 * CONSTRUCTOR Client version
	 */
	public Networking(Logging passedLog, int passedPort, String target) {
		int port = 40000;
		if ((passedPort > 1024) && (passedPort <= 65535)) {
			port = passedPort;
		}
		mylog = passedLog;
		try {
			ClientSocket = new Socket(target, port);
		} catch (UnknownHostException e) {
			mylog.out("FATAL", "Unknown host [" + target + "]");
		} catch (IOException e) {
			mylog.out("FATAL", "Unable to connect to target server and/or port");
		}
	}

	/**
	 * Passes the ClientSocket outside of the Networking class
	 * 
	 * @return
	 */
	public Socket PassBackClient() {
		return ClientSocket;
	}

	/**
	 * Binds to a port to listen for new connections
	 */
	private void BindServer(int passedSocket) {
		int socket = 8080;
		if ((passedSocket > 1024) && (passedSocket <= 65535)) {
			socket = passedSocket;
		} else {
			mylog.out("WARN", "Passed port number is out of bounds or in privledged space, defaulting to 8080.");
		}
		try {
			serverSocket = new ServerSocket(socket);
		} catch (IOException e) {
			mylog.out("FATAL", "Could not listen on port. Port probably in use.");
			System.exit(0);
		}
		mylog.out("INFO", "Now listening on port " + socket);
	}

	/**
	 * Listens on the servers port for new connections
	 * 
	 * @return the socket of the new connection
	 */
	public Socket ListenForNewConnection() {
		Socket newClient = null;
		try {
			newClient = serverSocket.accept();
		} catch (IOException e) {
			mylog.out("ERROR", "Error: Failed to establish connection with the new client.");
		}

		// Logging
		SocketAddress theirAddress = newClient.getRemoteSocketAddress();
		SocketAddress myAddress = newClient.getLocalSocketAddress();
		mylog.out("INFO", "A client from [" + theirAddress + "] has connected to [" + myAddress
				+ "] and has established a new session.");

		return newClient;
	}

	/**
	 * Connects IO to socket
	 * 
	 * @param passedSocket
	 */
	public void BringUp(Socket passedSocket) {
		// Bind input/output to the socket
		try {
			receive = new DataInputStream(passedSocket.getInputStream());
		} catch (NullPointerException e1) {
			mylog.out("ERROR", "Failed to setup RECEIVE input stream");
		} catch (IOException e2) {
			mylog.out("ERROR", "Failed to setup RECEIVE input stream");
		}
		try {
			send = new DataOutputStream(passedSocket.getOutputStream());
		} catch (IOException e) {
			mylog.out("ERROR", "Failed to setup SEND output stream");
		}
	}

	/**
	 * Tears down IO connected to socket
	 */
	public void BringDown() {
		// Close opened IO interfaces
		try {
			receive.close();
		} catch (IOException e) {
			mylog.out("ERROR", "Failed to close RECIEVE");
		}
		try {
			send.close();
		} catch (IOException e) {
			mylog.out("ERROR", "Failed to close SEND");
		}
	}

	/**
	 * Sends data over socket DATA TYPE: string UTF safe
	 * 
	 * @param data
	 */
	public void Send(String data) {
		try {
			send.writeUTF(data);
		} catch (IOException e1) {
			mylog.out("ERROR", "Failed to SEND data STRING");
		}
		try {
			send.flush();
		} catch (IOException e) {
			mylog.out("ERROR", "Failed to flush SEND buffer");
		}
	}

	/**
	 * Sends data over socket DATA TYPE: byte[]
	 * 
	 * @param data
	 */
	public void Send(byte[] data) {
		try {
			send.write(data);
			if (VERBOSE) {
				mylog.out("INFO", "Wrote [" + data.length + "] bytes.");
			}
		} catch (IOException e1) {
			mylog.out("ERROR", "Failed to SEND data byte[]");
		}
		try {
			send.flush();
		} catch (IOException e) {
			mylog.out("ERROR", "Failed to flush SEND buffer");
		}
	}

	/**
	 * Receives data over socket DATA TYPE: string UTF safe
	 * 
	 * @param data
	 * @throws InterruptedException
	 */
	public String Receive() throws InterruptedException {
		String fetched = null;
		int measure = 0;

		// Try to see if there is somthing to read BEFORE blocking to read
		// If there is nothing to receive yet, wait 1/2 a second and try again
		// for the next 5 seconds (10 times)

		for (int loop = 0; loop < 10; loop++) {
			try {
				measure = receive.available();
			} catch (IOException e) {
				mylog.out("ERROR", "Failed to mesure for RECEIVE data STRING");
			}

			if (measure == 0) {
				Thread.sleep(500);
			} else {
				break;
			}
		}

		// Even after waiting, we could not get any data - as such we will not
		// block "forever" in a listening state
		if (measure == 0) {
			return fetched;
		}

		// If we are here, then we SHOULD be able to read somthing
		try {
			fetched = receive.readUTF();
		} catch (IOException e1) {
			mylog.out("ERROR", "Failed to RECEIVE data STRING");
			fetched = null;
		}
		return fetched;
	}

	/**
	 * Receives data over socket DATA TYPE: byte[]
	 * 
	 * @param data
	 */
	public byte[] ReceiveByte() {
		// Prep
		int read = 0;
		byte[] fetched = null;

		// Wait (block) for data
		try {
			while (receive.available() == 0) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					mylog.out("WARN", "Failed to sleep while waiting for data over the network.");
				}
			}
		} catch (IOException e2) {
			mylog.out("WARN", "Failed to determine if data would/was arriving on the network so we could wait for it.");
		}

		// Data has arrived
		try {
			fetched = new byte[receive.available()];
			try {
				read = receive.read(fetched);
				if (VERBOSE) {
					mylog.out("INFO", "Read [" + read + "] bytes.");
				}
			} catch (IOException e1) {
				mylog.out("ERROR", "Failed to RECEIVE data STRING");
				fetched = null;
			}
		} catch (IOException e) {
			mylog.out("ERROR", "Failed to determine size of inbound data in bytes");
		} finally {
			if (read <= 0) {
				mylog.out("WARN", "Failed to read anything from the input stream.");
			}
		}
		return fetched;
	}

	/**
	 * Receives data over socket DATA TYPE: byte[]
	 * 
	 * @param data
	 */
	public byte[] ReceiveByteACK() {
		// Prep
		int read = 0;
		byte[] fetched = null;

		// Wait (block) for data
		try {
			while (receive.available() == 0) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					mylog.out("ERROR", "Failed to sleep while waiting for data over the network.");
				}
			}
		} catch (IOException e2) {
			mylog.out("ERROR", "Failed to determine if data would/was arriving on the network so we could wait for it.");
		}

		// Data has arrived
		try {
			fetched = new byte[32];
			try {
				read = receive.read(fetched, 0, 32);
				if (VERBOSE) {
					mylog.out("INFO", "Read [" + read + "] bytes.");
				}
			} catch (IOException e1) {
				mylog.out("ERROR", "Failed to RECEIVE data STRING");
				fetched = null;
			}
		} finally {
			if (read <= 0) {
				mylog.out("ERROR", "Failed to read anything from the input stream.");
			}
		}
		return fetched;
	}
}
