package com.spiddekauga.voider.repo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.utils.Buffers;

/**
 * Network gateway for sending HTTP request to the server
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
class NetworkGateway {
	/**
	 * Sends bytes over HTTP to the specified server
	 * @param url the server to send the request to
	 * @param entity the entity to send (as bytes)
	 * @return entity bytes response from the server.
	 * Length 0 if no response was found. null if an error occurred.
	 */
	public static byte[] sendRequest(String url, byte[] entity) {
		return sendRequest(url, entity, null);
	}

	/**
	 * Sends a file over HTTP to the specified server
	 * @param methodName the server to send the request to
	 * @param entity data to send
	 * @param file optional file to send
	 * @return entity bytes response from the server.
	 * Length 0 if no response was found. null if an error occurred.
	 */
	public static byte[] sendRequest(String methodName, byte[] entity, File file) {
		if (mHttpClient == null) {
			mHttpClient = HttpClients.createDefault();
		}

		try {
			MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
			entityBuilder.addBinaryBody(ENTITY_NAME, entity);

			// Add optional file
			if (file != null) {
				ContentBody contentBody = new FileBody(file);
				entityBuilder.addPart(BLOB_NAME, contentBody);
			}


			HttpPost httpPost = new HttpPost(SERVER_HOST + methodName);
			httpPost.setEntity(entityBuilder.build());

			CloseableHttpResponse httpResponse = mHttpClient.execute(httpPost);
			return getEntity(httpResponse);
		} catch (IOException e) {
			Gdx.app.log("Network", "Could not connect to server");
		}

		return null;
	}

	/**
	 * Reads the entity bytes from a HTTP response
	 * @param response the HTTP response to read the bytes from
	 * @return the entity bytes. Length 0 if no response was found. null if
	 * an error occurred.
	 */
	private static byte[] getEntity(CloseableHttpResponse response) {
		try {
			HttpEntity httpEntity = response.getEntity();
			InputStream entityStream = httpEntity.getContent();
			return Buffers.readBytes(entityStream);

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/** Server host */
	private static final String SERVER_HOST = "http://localhost:8888/";
	/** HTTP Client for the client side */
	private static CloseableHttpClient mHttpClient = null;

	/** Entity post name */
	private static final String ENTITY_NAME = "entity";
	/** Blob post name */
	private static final String BLOB_NAME = "fileKey";
}
