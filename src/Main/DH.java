package Main;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.DHParameterSpec;

/**
 * Handles Diffie Hellman Exchange to generate a shared secret
 * 
 * @author Cole Christie
 * 
 *         CITATION: This was used for inspiration:
 *         http://www.java2s.com/Tutorial
 *         /Java/0490__Security/DiffieHellmanKeyAgreement.htm
 */
public class DH {
	private Logging mylog;
	private BigInteger prime;
	private BigInteger base;
	private BigInteger sharedSecret;
	private DHParameterSpec dhParameters;
	private KeyPairGenerator keyGen;
	private KeyAgreement keyAgree;
	private KeyPair keyPair;

	/**
	 * CONTRUCTOR Generates a PRIME and a BASE
	 */
	public DH(Logging passedLog) {
		mylog = passedLog;

		GeneratePrime(1024);
		GenerateBase(128);
		//System.out.println("1024 bit Prime (Hex): " + GetPrime(16));
		//System.out.println("128 bit Base (Hex): " + GetBase(16));
	}

	/**
	 * CONTRUCTOR USES a pre-calculated prime and base
	 */
	public DH(Logging passedLog, String passedPrime, int radixPrime,
			String passedBase, int radixBase) {
		mylog = passedLog;
		prime = new BigInteger(passedPrime, radixPrime);
		base = new BigInteger(passedBase, radixBase);
	}

	/**
	 * Generate a new prime
	 * 
	 * @param bitLength
	 */
	public void GeneratePrime(int bitLength) {
		prime = new BigInteger(bitLength, 0, new SecureRandom());
	}

	/**
	 * Set the prime to a specific value
	 * 
	 * @param passedPrime
	 */
	public void SetPrime(String passedPrime, int radix) {
		prime = new BigInteger(passedPrime, radix);
	}

	/**
	 * Returns the prime as a string representation of base RADIX (base 2,10,16
	 * are popular radix)
	 * 
	 * @param radix
	 * @return
	 */
	public String GetPrime(int radix) {
		return prime.toString(radix);
	}

	/**
	 * Generate a new base
	 * 
	 * @param bitLength
	 */
	public void GenerateBase(int bitLength) {
		base = new BigInteger(bitLength, new SecureRandom());
	}

	/**
	 * Set the base to a specific value
	 * 
	 * @param passedPrime
	 */
	public void SetBase(String passedPrime, int radix) {
		base = new BigInteger(passedPrime, radix);
	}

	/**
	 * Returns the base as a string representation of base RADIX (base 2,10,16
	 * are popular radix)
	 * 
	 * @param radix
	 * @return
	 */
	public String GetBase(int radix) {
		return base.toString(radix);
	}

	/**
	 * Returns the shared secret as a string representation of base RADIX (base
	 * 2,10,16 are popular radix)
	 * 
	 * @param radix
	 * @return
	 */
	public String GetSharedSecret(int radix) {
		return sharedSecret.toString(radix);
	}

	/**
	 * Returns the public key
	 * 
	 * @return
	 */
	public PublicKey GetPublicKey() {
		PublicKey pubKey = keyPair.getPublic();
		return pubKey;
	}

	/**
	 * Returns the public key in byte form
	 * 
	 * @return
	 */
	public byte[] GetPublicKeyBF() {
		PublicKey pubKey = keyPair.getPublic();
		byte[] byteForm = pubKey.getEncoded();
		// String format = pubKey.getFormat();
		// System.out.println("Public Key in [" + format + "] : " + byteForm);
		return byteForm;
	}

	/**
	 * Returns a public key built from a passed byte[]
	 * 
	 * @return
	 */
	public PublicKey CraftPublicKey(byte[] source) {
		PublicKey pubKey = null;
		try {
			pubKey = KeyFactory.getInstance("DH").generatePublic(
					new X509EncodedKeySpec(source));
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			mylog.out("ERROR","Failed to build a public key from the received byte array.");
		}
		return pubKey;
	}

	/**
	 * Performs phase 1 of a Diffie Hellmankeying agreement
	 */
	public void DHPhase1() {
		// Prep DH
		dhParameters = new DHParameterSpec(prime, base);

		// Setup key generator
		keyGen = null;
		try {
			keyGen = KeyPairGenerator.getInstance("DH");
		} catch (NoSuchAlgorithmException e) {
			mylog.out("ERROR","There is no DH algorithm available.");
		}
		try {
			keyGen.initialize(dhParameters, new SecureRandom());
		} catch (InvalidAlgorithmParameterException e) {
			mylog.out("ERROR","Unable to intialize Key Generator.");
		}

		// Setup keying agreement
		keyAgree = null;
		try {
			keyAgree = KeyAgreement.getInstance("DH");
		} catch (NoSuchAlgorithmException e) {
			mylog.out("ERROR","Unable to instantiate Key Agreement.");
		}
		keyPair = keyGen.generateKeyPair();

		try {
			keyAgree.init(keyPair.getPrivate());
		} catch (InvalidKeyException e) {
			mylog.out("ERROR","Unable to calculate a private key for DH.");
		}
	}

	/**
	 * Performs phase 2 of a Diffie Hellmankeying agreement
	 */
	public void DHPhase2(PublicKey partnersKey) {
		try {
			keyAgree.doPhase(partnersKey, true);
		} catch (InvalidKeyException | IllegalStateException e) {
			mylog.out("ERROR","Unable to complete keying agreement.");
		}

		// Generate shared Secret
		byte[] secretBYTE = keyAgree.generateSecret();
		sharedSecret = new BigInteger(secretBYTE);
	}
}
