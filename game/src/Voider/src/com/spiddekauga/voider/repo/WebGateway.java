package com.spiddekauga.voider.repo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

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
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class WebGateway {
	/**
	 * Sends bytes over HTTP to the specified server
	 * @param methodName the method name in teh server
	 * @param entity the entity to send (as bytes)
	 * @return entity bytes response from the server.
	 * Length 0 if no response was found. null if an error occurred.
	 */
	public static byte[] sendRequest(String methodName, byte[] entity) {
		return sendUploadRequest(SERVER_HOST + methodName, entity, null);
	}

	/**
	 * Sends an entity and optional files over HTTP
	 * @param uploadUrl where to upload the files
	 * @param entity data to send
	 * @param files optional files to upload
	 * @return entity bytes response from the server.
	 * Length 0 if no response was found. null if an error occurred.
	 */
	public static byte[] sendUploadRequest(String uploadUrl, byte[] entity, ArrayList<FieldNameFileWrapper> files) {
		initHttpClient();

		try {
			MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
			entityBuilder.addBinaryBody(ENTITY_NAME, entity);

			// Add files
			if (files != null) {
				for (FieldNameFileWrapper fieldNameFile : files) {
					ContentBody contentBody = new FileBody(fieldNameFile.file);
					entityBuilder.addPart(fieldNameFile.fieldName, contentBody);
				}
			}


			HttpPost httpPost = new HttpPost(uploadUrl);
			httpPost.setEntity(entityBuilder.build());

			CloseableHttpResponse httpResponse = mHttpClient.execute(httpPost);
			byte[] responseEntity = getEntity(httpResponse);
			httpResponse.close();
			return responseEntity;
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

	/**
	 * Initializes HTTP Client
	 */
	private static void initHttpClient() {
		if (mHttpClient == null) {
			mHttpClient = HttpClients.custom()
					.setMaxConnTotal(10)
					.setMaxConnPerRoute(10)
					.build();
		}
	}

	/**
	 * Wrapper class for a file and field name
	 */
	static class FieldNameFileWrapper {
		/** Field name in the form */
		public String fieldName = null;
		/** The file to upload */
		public File file = null;
	}

	/** Server host */
	private static final String SERVER_HOST = "http://localhost:8888/";
	/** HTTP Client for the client side */
	private static CloseableHttpClient mHttpClient = null;

	/** Entity post name */
	private static final String ENTITY_NAME = "entity";
}
