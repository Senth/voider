package com.spiddekauga.voider.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

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

					if (item.getContentType().equals("application/octet-stream") && item.getFieldName().equals(ENTITY_NAME)) {
						mLogger.finer("Found entity");
						InputStream inputStream = item.openStream();
						return IOUtils.toByteArray(inputStream);
					}
				}

				// If we're here we haven't found the entity, try base64 instead
				String base64Entity = request.getParameter(ENTITY_NAME);
				if (base64Entity != null) {
					mLogger.finer("Found Base64 entity");
					return DatatypeConverter.parseBase64Binary(base64Entity);
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
			outputStream.write(entity);
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			String exceptionString = Strings.exceptionToString(e);
			mLogger.severe(exceptionString);
		}
	}

	/** Logger */
	private static final Logger mLogger = Logger.getLogger(NetworkGateway.class.getName());
	/** Entity post name */
	private static final String ENTITY_NAME = "entity";
}
