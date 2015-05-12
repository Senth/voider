package com.spiddekauga.voider.repo;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Base64Coder;
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
		else if (object instanceof char[]) {
			char[] base64 = (char[]) object;
			String string = new String(base64);
			writer.append("Content-Type: text/plain; charset=" + CHARSET).append(CRLF);
			writer.append(CRLF).append(string);
		}
		// Files
		else if (object instanceof File) {
			File file = (File) object;
			writer.append("Content-Type: application/octet-stream").append(CRLF);
			writer.append("Content-Transfer-Encoding: binary").append(CRLF);
			writer.append(CRLF).flush();
			DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
			byte[] bytes = new byte[(int) file.length()];
			dataInputStream.readFully(bytes);
			dataInputStream.close();
			output.write(bytes);
			output.flush();
		}
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
			PrintWriter writer = getPrintWriter(output);

			// Add files
			if (files != null && !files.isEmpty()) {
				for (FieldNameFileWrapper fieldNameFile : files) {
					addContent(output, writer, fieldNameFile.mFieldName, fieldNameFile.mFile, fieldNameFile.mFieldName);
				}

				// Add method entity as base64
				addContent(output, writer, ENTITY_NAME, Base64Coder.encode(entity), null);
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

			httpConnection.disconnect();

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

		try {
			HttpURLConnection httpConnection = getNewConnection(Config.Network.SERVER_HOST + methodName);
			OutputStream output = httpConnection.getOutputStream();
			PrintWriter writer = getPrintWriter(output);

			// Add method entity
			addContent(output, writer, ENTITY_NAME, entity, null);
			endContent(writer);

			// Write the response to a file
			BufferedInputStream response = new BufferedInputStream(httpConnection.getInputStream());
			FileHandle file = Gdx.files.external(filePath);
			file.write(response, false);

			response.close();
			httpConnection.disconnect();

			return true;
		} catch (IOException e) {
			Gdx.app.log("Network", "Error downloading file " + filePath);
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Get a print writer for the HTTP output
	 * @param output output for the HTTP connection
	 * @return new PrintWriter
	 * @throws UnsupportedEncodingException
	 */
	private static PrintWriter getPrintWriter(OutputStream output) throws UnsupportedEncodingException {
		return new PrintWriter(new OutputStreamWriter(output, CHARSET));
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

		return httpConnection;
	}

	/**
	 * Wrapper class for a file and field name
	 */
	public static class FieldNameFileWrapper {
		/**
		 * @param fieldName name of the field
		 * @param file the file to upload
		 */
		public FieldNameFileWrapper(String fieldName, File file) {
			mFieldName = fieldName;
			mFile = file;
		}

		/** Field name in the form */
		private String mFieldName;
		/** The file to upload */
		private File mFile;

	}


	private static final String ENTITY_NAME = "entity";
	private static final String CHARSET = StandardCharsets.UTF_8.name();
	private static final String BOUNDARY = UUID.randomUUID().toString();
	private static final String CRLF = "\r\n";
}
