package Main;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import org.apache.shiro.codec.CodecSupport;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.crypto.CryptoException;

/**
 * Handles basic encryption decryption
 * 
 * @author Cole Christie
 * 
 */
public class Crypto {
	private Logging mylog;
	private AesCipherService cipher;
	private byte[] KeyBytes;

	/**
	 * CONSTRUCTOR
	 */
	public Crypto(Logging passedLog, String password) {
		// Setup log
		mylog = passedLog;

		// Setup cipher
		cipher = new AesCipherService();

		// NEW key setup
		cipher.setKeySize(128);
		byte[] salt = CodecSupport.toBytes(password + "ExtraSalty");
		SecretKeyFactory factory = null;
		try {
			factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		} catch (NoSuchAlgorithmException e) {
			mylog.out("ERROR","PBKDF2 is MISSING");
		}
		KeySpec keyspec = new PBEKeySpec(password.toCharArray(), salt, 10, 128);
		Key key = null;
		try {
			key = factory.generateSecret(keyspec);
		} catch (InvalidKeySpecException e) {
			mylog.out("ERROR","Failed to generate secret key");
		}
		KeyBytes = key.getEncoded();

		// Log what is active
		int keySize = cipher.getKeySize();
		String cryptoAlg = cipher.getAlgorithmName();
		String cryptoMode = cipher.getModeName();
		mylog.out("INFO","Using " + keySize + " bit key with " + cryptoAlg + " in "
				+ cryptoMode + " mode.");
	}

	/**
	 * Rekey's the encryption channel with a new secret key
	 * @param newPassword
	 */
	public void ReKey(String newPassword) {
		byte[] salt = CodecSupport.toBytes(newPassword + "ExtraSalty");
		SecretKeyFactory factory = null;
		try {
			factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		} catch (NoSuchAlgorithmException e) {
			mylog.out("ERROR","PBKDF2 is MISSING");
		}
		KeySpec keyspec = new PBEKeySpec(newPassword.toCharArray(), salt, 10,
				128);
		Key key = null;
		try {
			key = factory.generateSecret(keyspec);
		} catch (InvalidKeySpecException e) {
			mylog.out("ERROR","Failed to generate secret key");
		}
		KeyBytes = key.getEncoded();
		mylog.out("INFO","Encryption rekeyed");
	}

	/**
	 * Simple proof of concept
	 */
	public void test() {
		String text = "Tell nobody!";

		// Encrypt
		byte[] encrypted = encrypt(text);

		// Decrypt
		String decrypted = decrypt(encrypted);

		// Validate
		if (text.compareTo(decrypted) == 0) {
			System.out.println("TRUE!");
		} else {
			System.out.println("False");
		}
		System.out.println("[" + text + "][" + decrypted + "]");
	}

	/**
	 * Returns the encrypted ByteSource of a passed plain text string
	 * 
	 * @param plainText
	 * @return
	 */
	public byte[] encrypt(String plainText) {
		byte[] encrypted = cipher.encrypt(CodecSupport.toBytes(plainText),
				KeyBytes).getBytes();
		return encrypted;
	}

	/**
	 * Returns the encrypted ByteSource of a passed byte[]
	 * 
	 * @param byteArray
	 * @return
	 */
	public byte[] encrypt(byte[] byteArray) {
		byte[] encrypted = cipher.encrypt(byteArray, KeyBytes).getBytes();
		return encrypted;
	}

	/**
	 * Returns the decrypted string of a passed encrypted ByteSource
	 * 
	 * @param plainText
	 * @return
	 */
	public String decrypt(byte[] encryptedText) {
		String decrypted = "Failed2DECRYPT";
		try {
			decrypted = CodecSupport.toString((cipher.decrypt(encryptedText,
					KeyBytes).getBytes()));
		} catch (CryptoException err) {
			mylog.out("WARN","Failed to decrypt the message. Likely bad PSK.");
		}
		return decrypted;
	}

	/**
	 * Returns the decrypted byte[] of a passed encrypted ByteSource
	 * 
	 * @param plainText
	 * @return
	 */
	public byte[] decryptByte(byte[] encryptedText) {
		byte[] decrypted = null;
		try {
			decrypted = (cipher.decrypt(encryptedText, KeyBytes).getBytes());
		} catch (CryptoException err) {
			mylog.out("WARN","Failed to decrypt the message. Likely bad PSK.");
		}
		return decrypted;
	}

	/**
	 * Test Diffie Hellman code
	 */
	public void testDH() {
		DH sideA = new DH(mylog);
		DH sideB = new DH(mylog, sideA.GetPrime(16), 16, sideA.GetBase(16), 16);
		sideA.DHPhase1();
		sideB.DHPhase1();
		sideA.DHPhase2(sideB.GetPublicKey());
		sideB.DHPhase2(sideA.GetPublicKey());

		// Final verification
		System.out.println("Shared Secret (Hex): " + sideA.GetSharedSecret(16));
		System.out.println("Shared Secret (Hex): " + sideB.GetSharedSecret(16));
	}
}
