package com.spiddekauga.prototype;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.ParamPart;
import com.oreilly.servlet.multipart.Part;

/**
 * Gateway for getting entities and sending an entity response
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class NetworkGateway {
	/**
	 * Reads the entity bytes from a server request
	 * @param request the HTTP server request to read the bytes from
	 * @return entity bytes. null if none was found.
	 */
	public static byte[] getEntity(HttpServletRequest request) {
		try {
			MultipartParser multipartParser = new MultipartParser(request, request.getContentLength());
			if (multipartParser != null) {
				Part part;
				while ((part = multipartParser.readNextPart()) != null) {
					if (part.getName().equals(ENTITY_NAME)) {
						if (part instanceof ParamPart) {
							return ((ParamPart) part).getValue();
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
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
			e.printStackTrace();
		}
	}

	/** Entity post name */
	private static final String ENTITY_NAME = "entity";
}
