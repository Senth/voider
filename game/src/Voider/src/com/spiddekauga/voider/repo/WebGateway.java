package com.spiddekauga.voider.repo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.MultipartEntityWithProgressBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.utils.IOutstreamProgressListener;
import com.spiddekauga.voider.Config;

/**
 * Network gateway for sending HTTP request to the server
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class WebGateway {
	/**
	 * Sends bytes over HTTP to the specified server
	 * @param methodName the method name in the server
	 * @param entity the entity to send (as bytes)
	 * @return entity bytes response from the server. Length 0 if no response was found. null if an error occurred.
	 */
	public static byte[] sendRequest(String methodName, byte[] entity) {
		return sendRequest(Config.Network.SERVER_HOST + methodName, entity, (List<FieldNameFileWrapper>) null,
				(List<IOutstreamProgressListener>) null);
	}

	/**
	 * Sends an entity over HTTP to the specified server
	 * @param uploadUrl where to upload the files
	 * @param entity data to send
	 * @param progressListeners optional progress listeners
	 * @return entity bytes response from the server. Length 0 if no response was found. null if an error occurred.
	 */
	public static byte[] sendRequest(String uploadUrl, byte[] entity, List<IOutstreamProgressListener> progressListeners) {
		return sendRequest(uploadUrl, entity, null, progressListeners);
	}

	/**
	 * Sends an entity over HTTP to the specified server
	 * @param uploadUrl where to upload the files
	 * @param entity data to send
	 * @param progressListener optional progress listener
	 * @return entity bytes response from the server. Length 0 if no response was found. null if an error occurred.
	 */
	public static byte[] sendRequest(String uploadUrl, byte[] entity, IOutstreamProgressListener progressListener) {
		return sendRequest(uploadUrl, entity, null, progressListener);
	}

	/**
	 * Sends an entity and optional files over HTTP
	 * @param uploadUrl where to upload the files
	 * @param entity data to send
	 * @param files optional files to upload
	 * @return entity bytes response from the server. Length 0 if no response was found. null if an error occurred.
	 */
	public static byte[] sendRequest(String uploadUrl, byte[] entity, ArrayList<FieldNameFileWrapper> files) {
		return sendRequest(uploadUrl, entity, files);
	}

	/**
	 * Sends an entity and optional files over HTTP
	 * @param uploadUrl where to upload the files
	 * @param entity data to send
	 * @param files optional files to upload
	 * @param progressListener optional progress listeners
	 * @return entity bytes response from the server. Length 0 if no response was found. null if an error occurred.
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
	 * @param progressListeners optional progress listeners
	 * @return entity bytes response from the server. Length 0 if no response was found. null if an error occurred.
	 */
	public static byte[] sendRequest(String uploadUrl, byte[] entity, List<FieldNameFileWrapper> files,
			List<IOutstreamProgressListener> progressListeners) {
		initHttpClient();

		try {
			MultipartEntityWithProgressBuilder entityBuilder = MultipartEntityWithProgressBuilder.create();
			Charset charset = Charset.forName("UTF-8");
			entityBuilder.setCharset(charset);


			// Add files
			if (files != null) {
				for (FieldNameFileWrapper fieldNameFile : files) {
					ContentBody contentBody = new FileBody(fieldNameFile.file);
					entityBuilder.addPart(fieldNameFile.fieldName, contentBody);
				}

				// Add binary body as BASE64
				String base64 = DatatypeConverter.printBase64Binary(entity);
				entityBuilder.addTextBody(ENTITY_NAME, base64, ContentType.TEXT_PLAIN);
			}
			// Add usual binary body
			else {
				entityBuilder.addBinaryBody(ENTITY_NAME, entity, ContentType.APPLICATION_OCTET_STREAM, null);
			}

			if (progressListeners != null) {
				entityBuilder.addProgressListener(progressListeners);
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
	 * Send a download request to the server
	 * @param methodName name of method to call on the server
	 * @param entity data to send, i.e. the method parameters
	 * @param filePath path to write the downloaded file to
	 * @return true if file was written successfully, false if an error occurred
	 */
	public static boolean downloadRequest(String methodName, byte[] entity, String filePath) {
		initHttpClient();

		CloseableHttpResponse httpResponse = null;

		try {
			String url = Config.Network.SERVER_HOST + methodName;
			MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
			entityBuilder.addBinaryBody(ENTITY_NAME, entity);

			HttpPost httpPost = new HttpPost(url);
			httpPost.setEntity(entityBuilder.build());

			httpResponse = mHttpClient.execute(httpPost);
		} catch (IOException e) {
			Gdx.app.log("Network", "Could not connect to server");
			e.printStackTrace();
		}

		if (httpResponse != null) {
			try {
				BufferedInputStream bis = new BufferedInputStream(httpResponse.getEntity().getContent());
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
				int inByte;
				while ((inByte = bis.read()) != -1) {
					bos.write(inByte);
				}
				bis.close();
				bos.close();

				httpResponse.close();
				return true;
			} catch (IOException e) {
				Gdx.app.log("Network", "Error downloading file");
				e.printStackTrace();
			}
		}


		return false;
	}

	/**
	 * Reads the entity bytes from a HTTP response
	 * @param response the HTTP response to read the bytes from
	 * @return the entity bytes. Length 0 if no response was found. null if an error occurred.
	 */
	private static byte[] getEntity(CloseableHttpResponse response) {
		try {
			return EntityUtils.toByteArray(response.getEntity());

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
			mHttpClient = HttpClients.custom().setMaxConnTotal(1).setMaxConnPerRoute(1).build();
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

	/** HTTP Client for the client side */
	private static CloseableHttpClient mHttpClient = null;

	/** Entity post name */
	private static final String ENTITY_NAME = "entity";
}
