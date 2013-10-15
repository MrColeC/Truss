package Main;

import java.io.IOException;
import java.net.Socket;

/**
 * Provides threads for the server so multiple clients can be handled by a
 * single server
 * 
 * @author Cole Christie
 * 
 */
public class ServerThread extends Thread {
	private Logging mylog;
	private Socket socket;
	private Networking network;
	private Auth subject;
	private int UID;
	private Crypto crypt;
	private Object JobLock;

	/**
	 * CONSTRUCTOR for Server Worker Thread
	 */
	public ServerThread(Auth passedSubject, Logging passedLog, Socket passedSocket, int passedUID,
			Object passedLock) {
		mylog = passedLog;
		socket = passedSocket;
		network = new Networking(mylog);
		subject = passedSubject;
		UID = passedUID;
		JobLock = passedLock;
	}

	/**
	 * Server thread Enables multi-client support
	 */
	public void run() {
		mylog.out("INFO", "Establishing session with client number [" + UID + "]");

		// Bind I/O to the socket
		network.BringUp(socket);

		// Prep
		String fromClient = null;

		// Activate crypto
		crypt = new Crypto(mylog, subject.GetPSK());
		byte[] fetched = network.ReceiveByte();
		String dec = crypt.decrypt(fetched);
		String craftReturn = dec + "<S>";
		mylog.out("INFO", "Validating encryption with handshake.");
		byte[] returnData = crypt.encrypt(craftReturn);
		network.Send(returnData);

		// Main Loop
		while (!socket.isClosed()) {
			// Collect data sent over the network
			fetched = network.ReceiveByte();
			if (fetched == null) {
				mylog.out("WARN", "Client disconnected abruptly");
				break;
			}

			// Decrypt sent data
			fromClient = crypt.decrypt(fetched);

			// Case sensitive actions based upon data received
			if (fromClient == null) {
				mylog.out("WARN", "Client disconnected abruptly");
				break;
			} else if (fromClient.compareToIgnoreCase("quit") == 0) {
				mylog.out("INFO", "Client disconnected gracefully");
				break;
			} else if (fromClient.compareToIgnoreCase("job") == 0) {
				mylog.out("INFO", "Client reuested a job.");
				synchronized (JobLock) {
					// TODO Implement sending a job
				}
			} else if (fromClient.compareToIgnoreCase("<REKEY>") == 0) {
				SendACK(); // Send ACK
				String prime = null;
				String base = null;

				// Grab 1st value (should be handshake for PRIME)
				fromClient = fromNetwork();
				SendACK(); // Send ACK
				if (fromClient.compareToIgnoreCase("<PRIME>") == 0) {
					prime = fromNetwork();
					SendACK(); // Send ACK
				} else {
					mylog.out("ERROR", "Failed proper DH handshake over the network (failed to receive PRIME).");
				}

				// Grab 2nd value (should be handshake for BASE)
				fromClient = fromNetwork();
				SendACK(); // Send ACK
				if (fromClient.compareToIgnoreCase("<BASE>") == 0) {
					base = fromNetwork();
					SendACK(); // Send ACK
				} else {
					mylog.out("ERROR", "Failed proper DH handshake over the network (failed to receive BASE).");
				}

				// Use received values to start DH
				DH myDH = new DH(mylog, prime, 16, base, 16);

				// Send rekeying ack
				returnData = crypt.encrypt("<REKEY-STARTING>");
				network.Send(returnData);
				RecieveACK(); // Wait for ACK

				// Perform phase1
				myDH.DHPhase1();

				// Receive client public key
				byte[] clientPubKey = null;
				fromClient = fromNetwork();
				SendACK(); // Send ACK
				if (fromClient.compareToIgnoreCase("<PUBLICKEY>") == 0) {
					clientPubKey = fromNetworkByte();
					SendACK(); // Send ACK
					returnData = crypt.encrypt("<PubKey-GOOD>");
					network.Send(returnData);
					RecieveACK(); // Wait for ACK
				} else {
					mylog.out("ERROR", "Failed to receieve client public key.");
				}

				// Send server public key to client
				network.Send(crypt.encrypt("<PUBLICKEY>"));
				RecieveACK(); // Wait for ACK
				network.Send(crypt.encrypt(myDH.GetPublicKeyBF()));
				RecieveACK(); // Wait for ACK
				fromClient = fromNetwork();
				SendACK(); // Send ACK
				if (fromClient.compareToIgnoreCase("<PubKey-GOOD>") != 0) {
					mylog.out("ERROR", "Client has failed to acknowledge server public key!");
				}

				// Use server DH public key to generate shared secret
				myDH.DHPhase2(myDH.CraftPublicKey(clientPubKey));

				// Final verification
				// System.out.println("Shared Secret (Hex): " +
				// myDH.GetSharedSecret(10));
				crypt.ReKey(myDH.GetSharedSecret(10));

			} else {
				mylog.out("INFO", "Received from client [" + fromClient + "]");
				craftReturn = "<S>" + fromClient;
				returnData = crypt.encrypt(craftReturn);
				network.Send(returnData);
			}
			try {
				Thread.sleep(1000); // Have the thread sleep for 1 second to
									// lower CPU load on the server
			} catch (InterruptedException e) {
				mylog.out("ERROR", "Failed to have the thread sleep.");
				e.printStackTrace();
			}
		}

		// Tear down bound I/O
		network.BringDown();

		// Close this socket
		try {
			socket.close();
		} catch (IOException e) {
			mylog.out("ERROR", "Failed to close SOCKET within SERVER THREAD");
		}
	}

	/**
	 * Reads a string from the network
	 * 
	 * @return
	 */
	private String fromNetwork() {
		String decryptedValue = null;

		byte[] initialValue = network.ReceiveByte();
		if (initialValue == null) {
			mylog.out("WARN", "Client disconnected abruptly");
		}
		decryptedValue = crypt.decrypt(initialValue);
		if (decryptedValue == null) {
			mylog.out("WARN", "Client disconnected abruptly");
		} else if (decryptedValue.compareToIgnoreCase("quit") == 0) {
			mylog.out("WARN", "Client disconnected abruptly");
		}

		return decryptedValue;
	}

	/**
	 * Read bytes from the network
	 * 
	 * @return
	 */
	private byte[] fromNetworkByte() {
		byte[] decryptedValue = null;

		byte[] initialValue = network.ReceiveByte();
		if (initialValue == null) {
			mylog.out("WARN", "Client disconnected abruptly");
		}
		decryptedValue = crypt.decryptByte(initialValue);
		if (decryptedValue == null) {
			mylog.out("WARN", "Client disconnected abruptly");
		}

		return decryptedValue;
	}

	/**
	 * Provides message synchronization
	 */
	private void SendACK() {
		network.Send(crypt.encrypt("<ACK>"));
		if (crypt.decrypt(network.ReceiveByteACK()).compareToIgnoreCase("<ACK>") != 0) {
			mylog.out("ERROR", "Partner failed to ACK");
		}
	}

	/**
	 * Provides message synchronization
	 */
	private void RecieveACK() {
		if (crypt.decrypt(network.ReceiveByteACK()).compareToIgnoreCase("<ACK>") != 0) {
			mylog.out("ERROR", "Partner failed to ACK");
		}
		network.Send(crypt.encrypt("<ACK>"));
	}
}
