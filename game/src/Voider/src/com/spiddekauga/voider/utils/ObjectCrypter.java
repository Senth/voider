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
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;

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
	public ObjectCrypter(SecretKey key) {
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
	 * @param obj the object to encrypt
	 * @return the encrypted object in bytes
	 * @see #decrypt(byte[])
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IOException
	 * @throws IllegalBlockSizeException
	 * @throws ShortBufferException
	 * @throws BadPaddingException
	 */
	public byte[] encrypt(Object obj) throws InvalidKeyException, InvalidAlgorithmParameterException, IOException, IllegalBlockSizeException, ShortBufferException, BadPaddingException {
		byte[] input = convertToByteArray(obj);
		mEnCipher.init(Cipher.ENCRYPT_MODE, mKey);

		return mEnCipher.doFinal(input);
	}

	/**
	 * Decrypts an array of bytes into an object.
	 * @param encrypted the encrypted byte array
	 * @return The decrypted object
	 * @see #encrypt(Object)
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Object decrypt( byte[]  encrypted) throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException, ClassNotFoundException {
		mDeCipher.init(Cipher.DECRYPT_MODE, mKey);

		return convertFromByteArray(mDeCipher.doFinal(encrypted));

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
	private SecretKey mKey;

}
