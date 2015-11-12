package com.spiddekauga.voider.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.spiddekauga.utils.Strings;

/**
 * Gateway for getting entities and sending an entity response
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class NetworkGateway {
	/**
	 * Reads the entity bytes from a server request
	 * @param request the HTTP server request to read the bytes from
	 * @return entity bytes. null if none was found.
	 */
	public static byte[] getEntity(HttpServletRequest request) {
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);

		if (isMultipart) {
			mLogger.finest("Multipart request");
			ServletFileUpload upload = new ServletFileUpload();
			try {
				FileItemIterator itemIt = upload.getItemIterator(request);

				while (itemIt.hasNext()) {
					FileItemStream item = itemIt.next();
					mLogger.finest("Found field: " + item.getFieldName());

					if (item.getFieldName().equals(ENTITY_NAME)) {
						// Binary
						if (item.getContentType().equals("application/octet-stream")) {
							mLogger.finer("Found binary entity");
							InputStream inputStream = item.openStream();
							return IOUtils.toByteArray(inputStream);
						}
						// Base 64
						else if (item.getContentType().contains("text/plain")) {
							mLogger.finer("Found Base64 entity");
							InputStream inputStream = item.openStream();
							String base64Entity = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
							return DatatypeConverter.parseBase64Binary(base64Entity);
						} else {
							mLogger.warning("Entity is neither binary nor base64");
						}
					}
				}

				// If we're here we haven't found the entity, try base64 instead
				String base64Entity = request.getParameter(ENTITY_NAME);
				if (base64Entity != null) {
					mLogger.finer("Found Base64 entity");
					return DatatypeConverter.parseBase64Binary(base64Entity);
				} else {
					mLogger.warning("Didn't find any entity");
				}
			} catch (FileUploadException | IOException e) {
				String exceptionString = Strings.exceptionToString(e);
				mLogger.severe(exceptionString);
			}
		} else {
			mLogger.warning("No multipart found");
		}

		return null;
	}

	/**
	 * Sends a response to the client
	 * @param response the HTTP response
	 * @param entity the byte entity to send (add to response)
	 */
	public static void sendResponse(HttpServletResponse response, byte[] entity) {
		try {
			OutputStream outputStream = response.getOutputStream();
			mLogger.finer("Writing entity of length " + entity.length);
			outputStream.write(entity);
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			String exceptionString = Strings.exceptionToString(e);
			mLogger.severe(exceptionString);
		}
	}

	/**
	 * Send message to the specified clients
	 * @param clientIds all clients to send the message to
	 * @param message the server message to send
	 */
	public static void sendMessage(Iterable<String> clientIds, String message) {
		for (String clientId : clientIds) {
			ChannelMessage channelMessage = new ChannelMessage(clientId, message);
			mChannelService.sendMessage(channelMessage);
		}
	}


	/** Channel service for sending messages */
	private static ChannelService mChannelService = ChannelServiceFactory.getChannelService();
	/** Logger */
	private static final Logger mLogger = Logger.getLogger(NetworkGateway.class.getName());
	/** Entity post name */
	private static final String ENTITY_NAME = "entity";
}
