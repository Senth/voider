package com.spiddekauga.network;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.xml.bind.DatatypeConverter;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.badlogic.gdx.Gdx;

/**
 * Network gateway for sending HTTP request to the server
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class WebGateway {
	/**
	 * Sends bytes over HTTP to the specified server
	 * @param methodName the method name in teh server
	 * @param entity the entity to send (as bytes)
	 * @return entity bytes response from the server. Length 0 if no response was found.
	 *         null if an error occurred.
	 */
	public static byte[] sendRequest(String methodName, byte[] entity) {
		return sendRequest(methodName, entity, null);
	}

	/**
	 * Sends a file over HTTP to the specified server
	 * @param methodName the server to send the request to
	 * @param entity data to send
	 * @param file optional file to send
	 * @return entity bytes response from the server. Length 0 if no response was found.
	 *         null if an error occurred.
	 */
	public static byte[] sendRequest(String methodName, byte[] entity, File file) {
		if (mHttpClient == null) {
			mHttpClient = HttpClients.custom().setMaxConnTotal(10).setMaxConnPerRoute(10).build();
		}

		try {
			MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
			Charset charset = Charset.forName("UTF-8");
			entityBuilder.setCharset(charset);


			// Add files
			if (file != null) {
				ContentBody contentBody = new FileBody(file);
				entityBuilder.addPart("file", contentBody);

				// Add binary body as BASE64
				String base64 = DatatypeConverter.printBase64Binary(entity);
				entityBuilder.addTextBody(ENTITY_NAME, base64, ContentType.TEXT_PLAIN);
			}
			// Add usual binary body
			else {
				entityBuilder.addBinaryBody(ENTITY_NAME, entity, ContentType.APPLICATION_OCTET_STREAM, null);
			}


			HttpPost httpPost = new HttpPost(SERVER_HOST + methodName);
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
	 * @return the entity bytes. Length 0 if no response was found. null if an error
	 *         occurred.
	 */
	private static byte[] getEntity(CloseableHttpResponse response) {
		try {
			return EntityUtils.toByteArray(response.getEntity());

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/** Server host */
	// private static final String SERVER_HOST = "http://localhost:8888/";
	private static final String SERVER_HOST = "http://voider-prototyp.appspot.com/";
	/** HTTP Client for the client side */
	private static CloseableHttpClient mHttpClient = null;

	/** Entity post name */
	private static final String ENTITY_NAME = "entity";
	/** Blob post name */
	private static final String BLOB_NAME = "fileKey";
}
