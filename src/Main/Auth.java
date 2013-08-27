package Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.ConfigurationException;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.slf4j.Logger;

/**
 * 
 * @author Cole Christie Purpose: Provides simple authentication interface
 */
public class Auth {
	private static Logger log;
	private static String psk;

	/**
	 * CONSTRUCTOR Sets up Shiro (pulls configuration from hard coded INI file
	 * currently
	 * 
	 * @param passedLog
	 */
	public Auth(Logger passedLog) {
		psk = null;
		log = passedLog;

		// Load Shiro
		try {
			Factory<SecurityManager> factory = new IniSecurityManagerFactory(
					"classpath:shiro.ini");
			SecurityManager securityManager = factory.getInstance();
			SecurityUtils.setSecurityManager(securityManager);
			log.info("Apache Shiro activated");
		} catch (ConfigurationException err) {
			log.warn("Failed to instantiate Apache Shiro\n" + err);
			System.exit(0);
		} catch (NoClassDefFoundError err) {
			log.warn("Failed to instantiate Apache Shiro\n" + err);
			System.exit(0);
		}
	}

	/**
	 * Establishes credentials based upon passed or provided input
	 * 
	 * @return
	 */
	public String[] GetCredential() {
		// Cast variables
		String user = "";
		String pw = "";

		// Create input handle
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		// Capture subject identity (user name)
		System.out.println("Provide subject identity:");
		try {
			user = br.readLine();
		} catch (IOException ioe) {
			log.warn("Failed to capture USERNAME input.");
		}

		// Capture password
		System.out.println("Provide subject password:");
		try {
			pw = br.readLine();
		} catch (IOException ioe) {
			log.warn("Failed to capture PASSWORD input.");
		}
		String[] Credentials = { user, pw }; // Cast return

		// Capture PSK (pre-shared key)
		System.out.println("Provide network pre-shared key:");
		try {
			psk = br.readLine();
		} catch (IOException ioe) {
			log.warn("Failed to capture PSK input.");
		}

		return Credentials;
	}

	/**
	 * Establishes an authenticated session based upon credentials provided
	 * 
	 * @param username
	 * @param password
	 */
	public Subject Login(String username, String password) {
		// Create subject identity
		Subject currentUser = SecurityUtils.getSubject();

		// Authenticate user
		if (!currentUser.isAuthenticated()) {
			try {
				UsernamePasswordToken token = new UsernamePasswordToken(
						username, password);
				token.setRememberMe(true); // Create token (SSO)
				currentUser.login(token);
			} catch (UnknownAccountException err) {
				// Bad user name
				failedLogin();
			} catch (IncorrectCredentialsException err) {
				// Bad password
				failedLogin();
			} catch (LockedAccountException err) {
				// account locked
				failedLogin();
			} catch (AuthenticationException err) {
				// other error
				failedLogin();
			}
		}

		// Log subject used
		log.info("Using [" + currentUser.getPrincipal() + "] credentials.");

		// Check permissions
		if (currentUser.hasRole("nothing")) {
			log.warn("Account has NO PRIVLEGES");
		} else {
			if (currentUser.hasRole("secureTarget")) {
				log.info("Jobs can be RECIEVED");
				log.info("PRIVATE jobs can be calculated");
			} else if (currentUser.hasRole("insecureTarget")) {
				log.info("Jobs can be RECIEVED");
				log.info("PUBLIC jobs can be calculated");
			} else if (currentUser.hasRole("sourceTarget")) {
				log.info("Job classification system ENABLED");
				log.info("Jobs can be SENT");
				log.info("Workers can be BOUND (Authenticated & Authorized)");
			} else if (currentUser.hasRole("resultTarget")) {
				log.info("Completed jobs (WORK) can be RECIEVED");
				log.info("Workers can be BOUND (Authenticated & Authorized)");
			}
		}
		return currentUser;
	}

	/**
	 * Exits the application when called
	 */
	private void failedLogin() {
		log.warn("Login DENIED");
		log.info("Loader Framework terminated");
		System.exit(0);
	}

	/**
	 * Setups a subject session and caches purpose (for simplified access)
	 * 
	 * @param targetSubject
	 * @return
	 */
	public Session EstablishSession(Subject targetSubject) {
		// Setup a session
		Session session = targetSubject.getSession();

		// Determine the rough use of the session, store it for easy access
		if (targetSubject.hasRole("nothing")) {
			session.setAttribute("USE", "");
		} else {
			if (targetSubject.hasRole("secureTarget")) {
				session.setAttribute("USE", "private");
			} else if (targetSubject.hasRole("insecureTarget")) {
				session.setAttribute("USE", "public");
			} else if (targetSubject.hasRole("sourceTarget")) {
				session.setAttribute("USE", "server");
			} else if (targetSubject.hasRole("resultTarget")) {
				session.setAttribute("USE", "dropoff");
			}
		}
		// Return created and annotated session
		return session;
	}

	/**
	 * Returns the PSK provided so it can be used for PBKDF2
	 * 
	 * @return
	 */
	public String GetPSK() {
		return psk;
	}
}
