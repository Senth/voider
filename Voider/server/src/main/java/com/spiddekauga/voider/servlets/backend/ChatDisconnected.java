package com.spiddekauga.voider.servlets.backend;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import com.google.appengine.api.datastore.Key;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.utils.Strings;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CConnectedUser;

/**
 * Called when a channel has been disconnected

 */
@SuppressWarnings("serial")
public class ChatDisconnected extends HttpServlet {
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String channelId = getChannelId(req);
		if (channelId != null) {
			removeChannelFromDatastore(channelId);
		}
		resp.setStatus(HttpServletResponse.SC_OK);
	}

	/**
	 * @param request the request
	 * @return clientID from the post data, null if not found
	 */
	private String getChannelId(HttpServletRequest request) {
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);

		if (isMultipart) {
			try {
				ServletFileUpload upload = new ServletFileUpload();

				FileItemIterator itemIt = upload.getItemIterator(request);
				while (itemIt.hasNext()) {
					FileItemStream item = itemIt.next();

					if (item.isFormField() && item.getFieldName().equals("from")) {
						return Streams.asString(item.openStream());
					}
				}
			} catch (IOException | FileUploadException e) {
				mLogger.severe("Error while reading post-data\n" + Strings.exceptionToString(e));
			}
		}

		return null;
	}

	/**
	 * Remove channel key when the user is disconnect
	 * @param channelId the channel to remove
	 */
	private void removeChannelFromDatastore(String channelId) {
		FilterWrapper channelFilter = new FilterWrapper(CConnectedUser.CHANNEL_ID, channelId);
		Key key = DatastoreUtils.getSingleKey(DatastoreTables.CONNECTED_USER, channelFilter);

		if (key != null) {
			DatastoreUtils.delete(key);
		}
	}

	private final Logger mLogger = Logger.getLogger(getClass().getSimpleName());
}
