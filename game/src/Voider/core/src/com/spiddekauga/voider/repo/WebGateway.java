package com.spiddekauga.voider.repo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.net.IOutstreamProgressListener;
import com.spiddekauga.voider.Config;

/**
 * Network gateway for sending HTTP request to the server
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class WebGateway {
	/**
	 * Sends bytes over HTTP to the specified server
	 * @param methodName the method name in the server
	 * @param entity the entity to send (as bytes)
	 * @return entity bytes response from the server. Length 0 if no response was found.
	 *         null if an error occurred.
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
	 * @return entity bytes response from the server. Length 0 if no response was found.
	 *         null if an error occurred.
	 */
	public static byte[] sendRequest(String uploadUrl, byte[] entity, List<IOutstreamProgressListener> progressListeners) {
		return sendRequest(uploadUrl, entity, null, progressListeners);
	}

	/**
	 * Sends an entity over HTTP to the specified server
	 * @param uploadUrl where to upload the files
	 * @param entity data to send
	 * @param progressListener optional progress listener
	 * @return entity bytes response from the server. Length 0 if no response was found.
	 *         null if an error occurred.
	 */
	public static byte[] sendRequest(String uploadUrl, byte[] entity, IOutstreamProgressListener progressListener) {
		return sendRequest(uploadUrl, entity, null, progressListener);
	}

	/**
	 * Sends an entity and optional files over HTTP
	 * @param uploadUrl where to upload the files
	 * @param entity data to send
	 * @param files optional files to upload
	 * @return entity bytes response from the server. Length 0 if no response was found.
	 *         null if an error occurred.
	 */
	public static byte[] sendRequest(String uploadUrl, byte[] entity, ArrayList<FieldNameFileWrapper> files) {
		return sendRequest(uploadUrl, entity, files, (IOutstreamProgressListener) null);
	}

	/**
	 * Sends an entity and optional files over HTTP
	 * @param uploadUrl where to upload the files
	 * @param entity data to send
	 * @param files optional files to upload
	 * @param progressListener optional progress listeners
	 * @return entity bytes response from the server. Length 0 if no response was found.
	 *         null if an error occurred.
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
	 * Add content to the HTTP request
	 * @param output output for the writer
	 * @param writer the writer which uses the output
	 * @param name name of the field
	 * @param object the object to write
	 * @param filename optional filename, set to null to skip
	 * @throws IOException
	 */
	private static void addContent(OutputStream output, PrintWriter writer, String name, Object object, String filename) throws IOException {
		writer.append("--" + BOUNDARY).append(CRLF);
		writer.append("Content-Disposition: form-data; name=\"" + ENTITY_NAME + "\"");
		if (filename != null && !filename.isEmpty()) {
			writer.append("; filename=\"").append(filename).append("\"");
		}
		writer.append(CRLF);

		// Method in byte array
		if (object instanceof byte[]) {
			writer.append("Content-Type: application/octet-stream").append(CRLF);
			writer.append("Content-Transfer-Encoding: binary").append(CRLF);
			writer.append(CRLF).flush();
			output.write((byte[]) object);
			output.flush();
		}
		// Method in Base64
		else if (object instanceof String) {
			writer.append("Content-Type: text/plain; charset=" + CHARSET).append(CRLF);
			writer.append(CRLF).append((String) object);
		}
		// Files
		else if (object instanceof File) {
			File file = (File) object;
			writer.append("Content-Type: application/octet-stream").append(CRLF);
			writer.append("Content-Transfer-Encoding: binary").append(CRLF);
			writer.append(CRLF).flush();
			Files.copy(file.toPath(), output);
			output.flush();
		}
		writer.append(CRLF).flush();

		// Add method entity as base64
		writer.append("--" + BOUNDARY).append(CRLF);
		writer.append("Content-Disposition: form-data; name=\"" + ENTITY_NAME + "\"").append(CRLF);

		writer.append(CRLF).flush();
	}

	/**
	 * End the content
	 * @param writer output to the request
	 */
	private static void endContent(PrintWriter writer) {
		writer.append("--" + BOUNDARY + "--").append(CRLF).flush();
	}

	/**
	 * Sends an entity and optional files over HTTP
	 * @param uploadUrl where to upload the files
	 * @param entity data to send
	 * @param files optional files to upload
	 * @param progressListeners optional progress listeners
	 * @return entity bytes response from the server. Length 0 if no response was found.
	 *         null if an error occurred.
	 */
	public static byte[] sendRequest(String uploadUrl, byte[] entity, List<FieldNameFileWrapper> files,
			List<IOutstreamProgressListener> progressListeners) {

		try {
			HttpURLConnection httpConnection = getNewConnection(uploadUrl);
			OutputStream output = httpConnection.getOutputStream();
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, CHARSET));

			// Add files
			if (files != null && !files.isEmpty()) {
				for (FieldNameFileWrapper fieldNameFile : files) {
					addContent(output, writer, fieldNameFile.fieldName, fieldNameFile.file, fieldNameFile.fieldName);
				}

				// Add method entity as base64
				addContent(output, writer, ENTITY_NAME, DatatypeConverter.printBase64Binary(entity), null);
			}
			// Add method entity
			else {
				addContent(output, writer, ENTITY_NAME, entity, null);
			}

			// End of multipart form data
			endContent(writer);

			// Get response
			InputStream response = httpConnection.getInputStream();
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			int n = -1;
			byte[] buffer = new byte[4096];
			while ((n = response.read(buffer)) != -1) {
				byteArrayOutputStream.write(buffer, 0, n);
			}

			if (mCookies == null) {
				mCookies = httpConnection.getHeaderFields().get("Set-Cookie");
			}

			return byteArrayOutputStream.toByteArray();
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

		// REMOVE
		// initHttpClient();
		//
		// CloseableHttpResponse httpResponse = null;
		//
		// try {
		// String url = Config.Network.SERVER_HOST + methodName;
		// MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
		// entityBuilder.addBinaryBody(ENTITY_NAME, entity);
		//
		// HttpPost httpPost = new HttpPost(url);
		// httpPost.setEntity(entityBuilder.build());
		//
		// // httpResponse = mHttpClient.execute(httpPost);
		// } catch (IOException e) {
		// Gdx.app.log("Network", "Could not connect to server");
		// e.printStackTrace();
		// }
		//
		// if (httpResponse != null) {
		// try {
		// BufferedInputStream bis = new
		// BufferedInputStream(httpResponse.getEntity().getContent());
		// BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new
		// File(filePath)));
		// int inByte;
		// while ((inByte = bis.read()) != -1) {
		// bos.write(inByte);
		// }
		//
		// bis.close();
		// bos.close();
		// httpResponse.close();
		//
		// return true;
		// } catch (IOException e) {
		// Gdx.app.log("Network", "Error downloading file");
		// e.printStackTrace();
		// }
		// }


		return false;
	}

	/**
	 * @param url the url to send the request to
	 * @return new HttpURLConnection with all default parameters set
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	private static HttpURLConnection getNewConnection(String url) throws MalformedURLException, IOException {
		HttpURLConnection httpConnection = (HttpURLConnection) new URL(url).openConnection();
		httpConnection.setDoOutput(true);
		httpConnection.setDoInput(true);
		httpConnection.setRequestProperty("Accept-Charset", CHARSET);
		httpConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

		if (mCookies != null) {
			for (String cookie : mCookies) {
				httpConnection.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
			}
		} else {
			CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
		}

		return httpConnection;
	}

	/**
	 * Wrapper class for a file and field name
	 */
	public static class FieldNameFileWrapper {
		/** Field name in the form */
		public String fieldName = null;
		/** The file to upload */
		public File file = null;
	}


	// Cookie information
	private static List<String> mCookies = null;

	private static final String ENTITY_NAME = "entity";
	private static final String CHARSET = StandardCharsets.UTF_8.name();
	private static final String BOUNDARY = UUID.randomUUID().toString();
	private static final String CRLF = "\r\n";
}
