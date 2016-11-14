package com.spiddekauga.voider.repo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Base64Coder;
import com.spiddekauga.http.HttpPostBuilder;
import com.spiddekauga.net.IOutstreamProgressListener;
import com.spiddekauga.voider.Config;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Network gateway for sending HTTP request to the server
 */
public class WebGateway {
private static final String ENTITY_NAME = "entity";
private static final String BLOB_KEY = "blob_key";


/**
 * Sends bytes over HTTP to the specified server
 * @param methodName the method name in the server
 * @param entity the entity to send (as bytes)
 * @return entity bytes response from the server. Length 0 if no response was found. null if an
 * error occurred.
 */
public static byte[] sendRequest(String methodName, byte[] entity) {
	return sendRequest(Config.Network.SERVER_HOST + methodName, entity, (List<FieldNameFileWrapper>) null,
			(List<IOutstreamProgressListener>) null);
}

/**
 * Sends an entity and optional files over HTTP
 * @param uploadUrl where to upload the files
 * @param entity data to send
 * @param files optional files to upload
 * @param progressListeners optional progress listeners
 * @return entity bytes response from the server. Length 0 if no response was found. null if an
 * error occurred.
 */
public static byte[] sendRequest(String uploadUrl, byte[] entity, List<FieldNameFileWrapper> files,
								 List<IOutstreamProgressListener> progressListeners) {

	try {
		HttpPostBuilder postBuilder = new HttpPostBuilder(uploadUrl);

		// Add files
		if (files != null && !files.isEmpty()) {
			postBuilder.doFileUpload();
			for (FieldNameFileWrapper fieldNameFile : files) {
				postBuilder.addFile(fieldNameFile.mFieldName, fieldNameFile.mFile);
			}
		}

		// Add method entity as base64
		char[] base64 = Base64Coder.encode(entity);
		postBuilder.addParameter(ENTITY_NAME, base64);


		HttpURLConnection connection = postBuilder.build();

		// Get response
		BufferedInputStream bufferedResponse = new BufferedInputStream(connection.getInputStream());
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		int n = -1;
		byte[] buffer = new byte[4096];
		while ((n = bufferedResponse.read(buffer)) != -1) {
			byteArrayOutputStream.write(buffer, 0, n);
		}


		connection.disconnect();

		byte[] responseEntity = byteArrayOutputStream.toByteArray();
		return responseEntity;
	} catch (IOException e) {
		Gdx.app.log("WebGateway", "Could not connect to server (" + uploadUrl + ")");
	}

	return null;
}

/**
 * Sends an entity over HTTP to the specified server
 * @param uploadUrl where to upload the files
 * @param entity data to send
 * @param progressListeners optional progress listeners
 * @return entity bytes response from the server. Length 0 if no response was found. null if an
 * error occurred.
 */
public static byte[] sendRequest(String uploadUrl, byte[] entity, List<IOutstreamProgressListener> progressListeners) {
	return sendRequest(uploadUrl, entity, null, progressListeners);
}

/**
 * Sends an entity over HTTP to the specified server
 * @param uploadUrl where to upload the files
 * @param entity data to send
 * @param progressListener optional progress listener
 * @return entity bytes response from the server. Length 0 if no response was found. null if an
 * error occurred.
 */
public static byte[] sendRequest(String uploadUrl, byte[] entity, IOutstreamProgressListener progressListener) {
	return sendRequest(uploadUrl, entity, null, progressListener);
}

/**
 * Sends an entity and optional files over HTTP
 * @param uploadUrl where to upload the files
 * @param entity data to send
 * @param files optional files to upload
 * @param progressListener optional progress listeners
 * @return entity bytes response from the server. Length 0 if no response was found. null if an
 * error occurred.
 */
public static byte[] sendRequest(String uploadUrl, byte[] entity, List<FieldNameFileWrapper> files, IOutstreamProgressListener progressListener) {
	ArrayList<IOutstreamProgressListener> progressListenersList = null;
	if (progressListener != null) {
		progressListenersList = new ArrayList<>();
		progressListenersList.add(progressListener);
	}
	return sendRequest(uploadUrl, entity, files, progressListenersList);
}

/**
 * Sends an entity and optional files over HTTP
 * @param uploadUrl where to upload the files
 * @param entity data to send
 * @param files optional files to upload
 * @return entity bytes response from the server. Length 0 if no response was found. null if an
 * error occurred.
 */
public static byte[] sendRequest(String uploadUrl, byte[] entity, ArrayList<FieldNameFileWrapper> files) {
	return sendRequest(uploadUrl, entity, files, (IOutstreamProgressListener) null);
}

/**
 * Send a download request to the server. Will retry to download the blob if there was an 500
 * error.
 * @param methodName name of method to call on the server
 * @param blobKey the blob to download
 * @param filePath path to write the downloaded file to
 * @return true if file was written successfully, false if an error occurred
 */
public static boolean downloadRequest(String methodName, String blobKey, String filePath) {
	int cTries = 1;
	boolean success = false;

	while (cTries <= Config.Network.RETRIES_MAX && !success) {

		try {
			HttpPostBuilder postBuilder = new HttpPostBuilder(Config.Network.SERVER_HOST + methodName);
			postBuilder.addParameter(BLOB_KEY, blobKey);

			HttpURLConnection connection = postBuilder.build();

			// Retry
			if (connection.getResponseCode() != HttpURLConnection.HTTP_INTERNAL_ERROR) {
				Gdx.app.debug(WebGateway.class.getSimpleName(),
						"Response code: " + connection.getResponseCode() + ": " + connection.getResponseMessage());
				// Write the response to a file
				BufferedInputStream response = new BufferedInputStream(connection.getInputStream());
				FileHandle file = Gdx.files.external(filePath);
				file.write(response, false);

				response.close();
				connection.disconnect();

				success = true;
			}
			// Retry
			else {
				cTries++;

				if (cTries <= Config.Network.RETRIES_MAX) {
					Gdx.app.log(WebGateway.class.getSimpleName(), "Try " + cTries + " to download " + filePath);
				} else {
					Gdx.app.error(WebGateway.class.getSimpleName(), "Out of tries for downloading " + filePath);
				}
			}
		} catch (IOException e) {
			Gdx.app.error(WebGateway.class.getSimpleName(), "Error downloading file " + filePath);
			e.printStackTrace();
			cTries = Integer.MAX_VALUE;
		}
	}

	return success;
}

/**
 * Wrapper class for a file and field name
 */
public static class FieldNameFileWrapper {
	/** Field name in the form */
	private String mFieldName;
	/** The file to upload */
	private File mFile;

	/**
	 * @param fieldName name of the field
	 * @param file the file to upload
	 */
	public FieldNameFileWrapper(String fieldName, File file) {
		mFieldName = fieldName;
		mFile = file;
	}

}
}
