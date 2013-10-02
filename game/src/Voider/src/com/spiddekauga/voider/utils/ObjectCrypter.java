package com.spiddekauga.voider.utils;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Encrypts and decrypts objects
 * 
 * @author sherif http://stackoverflow.com/users/446552/sherif
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ObjectCrypter {
	/**
	 * Creates an AES crypter with the specified key
	 * @param key the key to be used for the cipher
	 */
	public ObjectCrypter(SecretKeySpec key) {
		// create the cipher with the algorithm you choose
		// see javadoc for Cipher class for more info, e.g.
		try {
			mKey = key;
			mDeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			mEnCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Encrypts an object to a byte array.
	 * @param <EncryptType> encrypted type
	 * @param obj the object to encrypt
	 * @return the encrypted object in bytes
	 * @see #decrypt(byte[],Class)
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IOException
	 * @throws IllegalBlockSizeException
	 * @throws ShortBufferException
	 * @throws BadPaddingException
	 */
	public <EncryptType> byte[] encrypt(EncryptType obj) throws InvalidKeyException, InvalidAlgorithmParameterException, IOException, IllegalBlockSizeException, ShortBufferException, BadPaddingException {
		byte[] input = null;
		if (!(obj instanceof byte[])) {
			input = convertToByteArray(obj);
		} else {
			input = (byte[]) obj;
		}
		mEnCipher.init(Cipher.ENCRYPT_MODE, mKey);

		// Get IV for the encryption
		byte[] iv = mEnCipher.getIV();
		byte[] encryptedBytes = mEnCipher.doFinal(input);

		// Includes IV for decrypting
		// Concatenates both byte arrays to message
		byte[] ivAndEncrypted = new byte [iv.length + encryptedBytes.length];
		System.arraycopy(iv, 0, ivAndEncrypted, 0, iv.length);
		System.arraycopy(encryptedBytes, 0, ivAndEncrypted, iv.length, encryptedBytes.length);

		return ivAndEncrypted;
	}

	/**
	 * Decrypts an array of bytes into an object.
	 * @param <DecryptedType> the type to decrypt to, must be same as encrypted!
	 * @param encrypted the encrypted byte array
	 * @param decryptToType decrypts the message to this type
	 * @return The decrypted object
	 * @see #encrypt(Object)
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public <DecryptedType> DecryptedType decrypt(byte[] encrypted, Class<DecryptedType> decryptToType) throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException, ClassNotFoundException {
		// Get the IV from the byte array
		byte[] iv = new byte [IV_LENGTH];
		System.arraycopy(encrypted, 0, iv, 0, IV_LENGTH);

		// Get the actual encrypted message
		byte[] encryptedMessage = new byte [encrypted.length - IV_LENGTH];
		System.arraycopy(encrypted, IV_LENGTH, encryptedMessage, 0, encrypted.length - IV_LENGTH);

		mDeCipher.init(Cipher.DECRYPT_MODE, mKey, new IvParameterSpec(iv));

		byte[] decryptedMessage = mDeCipher.doFinal(encryptedMessage);

		if (decryptToType == byte[].class) {
			return (DecryptedType) decryptedMessage;
		} else {
			return (DecryptedType) convertFromByteArray(decryptedMessage);
		}
	}

	/**
	 * Converts a byte array back to the original object
	 * @param byteObject
	 * @return object recreated from the byte array
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private Object convertFromByteArray(byte[] byteObject) throws IOException,
	ClassNotFoundException {
		ByteArrayInputStream bais;

		ObjectInputStream in;
		bais = new ByteArrayInputStream(byteObject);
		in = new ObjectInputStream(bais);
		Object o = in.readObject();
		in.close();
		return o;

	}



	/**
	 * Converts an object into a byte array
	 * @param complexObject
	 * @return byte array of the converted object
	 * @throws IOException
	 */
	private byte[] convertToByteArray(Object complexObject) throws IOException {
		ByteArrayOutputStream baos;
		ObjectOutputStream out;

		baos = new ByteArrayOutputStream();
		out = new ObjectOutputStream(baos);
		out.writeObject(complexObject);
		out.close();

		return baos.toByteArray();

	}

	/** Deciphers an object */
	private Cipher mDeCipher;
	/** Enciphers an object */
	private Cipher mEnCipher;
	/** The secret key, not to be shared */
	private SecretKeySpec mKey;

	/** The initialization vector length */
	private static final int IV_LENGTH = 16;

}
