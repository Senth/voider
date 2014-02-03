package com.spiddekauga.prototype;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.magicscroll.server.blobstore.ChainedBlobstoreInputStream;

import com.esotericsoftware.kryo.io.Input;
import com.google.appengine.api.blobstore.BlobKey;
import com.spiddekauga.appengine.BlobUtils;
import com.spiddekauga.utils.ObjectCrypter;

/**
 * Enemy has been uploaded
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class EnemyUploadFinished extends HttpServlet {
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		mLogger.info("Upload done");

		List<BlobKey> uploadedBlobkeys = BlobUtils.getBlobKeysFromUpload(request);

		BlobKey blobKey = uploadedBlobkeys.get(0);

		mLogger.info("Blobkey: " + blobKey);

		ChainedBlobstoreInputStream blobInputStream = new ChainedBlobstoreInputStream(blobKey);

		ArrayList<byte[]> byteBuffers = new ArrayList<>();
		int cReadBytesTotal = 0;
		do {
			byte[] readBytes = new byte[BUFFER_SIZE];
			int cReadBytes = blobInputStream.read(readBytes, 0, BUFFER_SIZE);
			cReadBytesTotal += cReadBytes;

			// Read less than maximum, end of file
			if (cReadBytes < BUFFER_SIZE) {
				// Copy value to a smaller buffer and append it to byte buffers
				if (cReadBytes > 0) {
					byte[] smallBuffer = new byte[cReadBytes];
					System.arraycopy(readBytes, 0, smallBuffer, 0, cReadBytes);
					byteBuffers.add(smallBuffer);
				}

				break;
			} else {
				byteBuffers.add(readBytes);
			}

		} while (true);

		mLogger.info("Total read bytes: " + cReadBytesTotal);

		// Merge all to one big buffer
		byte[] encryptedObject = new byte[cReadBytesTotal];
		int offset = 0;
		for (byte[] partBuffer : byteBuffers) {
			System.arraycopy(partBuffer, 0, encryptedObject, offset, partBuffer.length);
			offset += partBuffer.length;
		}


		// Decrypt the enemy
		ObjectCrypter objectCrypter = CryptConfig.getCrypter();
		byte[] decryptedObject;
		try {
			decryptedObject = objectCrypter.decrypt(encryptedObject, byte[].class);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | IllegalArgumentException | ClassNotFoundException e) {
			e.printStackTrace();
			blobInputStream.close();
			return;
		}

		Input input = new Input(decryptedObject);


		blobInputStream.close();
	}

	/** Buffer size, 256kb */
	private static final int BUFFER_SIZE = 256 * 1024;
	/** Logger */
	private static final Logger mLogger = Logger.getLogger(EnemyUploadFinished.class.getName());
}
