package com.spiddekauga.voider.backup;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.utils.Base64Coder;
import com.spiddekauga.http.HttpPostBuilder;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.NetworkEntitySerializer;
import com.spiddekauga.voider.network.misc.GetUploadUrlMethod;
import com.spiddekauga.voider.network.misc.GetUploadUrlResponse;

/**
 * Interface for various actions
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
abstract class Action {
	/**
	 * Execute the action
	 */
	public abstract void execute();

	/**
	 * @return true if all arguments have been set
	 */
	public boolean isAllArgumentsSet() {
		return mUrl != null && !mUrl.isEmpty() && mBackupDir != null && !mBackupDir.isEmpty();
	}

	/**
	 * Set the backup directory
	 * @param backupDir backup directory
	 * @return true if successful, false if failed somehow
	 */
	protected boolean setBackupDir(String backupDir) {
		// Create backup directory
		if (!dirExists(backupDir)) {
			File file = new File(backupDir);
			try {
				file.mkdirs();
			} catch (SecurityException e) {
				return false;
			}
		}

		mBackupDir = backupDir;
		if (!mBackupDir.endsWith("/")) {
			mBackupDir += "/";
		}

		return true;
	}

	/**
	 * @return backup directory
	 */
	protected String getBackupDir() {
		return mBackupDir;
	}

	/**
	 * @return revision backup directory
	 */
	protected String getBackupRevDir() {
		return mBackupDir + "userResources/";
	}

	/**
	 * Set the URL of the action
	 * @param Url
	 */
	protected void setUrl(String Url) {
		mUrl = Url;

		// Always end with slash
		if (!mUrl.endsWith("/")) {
			mUrl += "/";
		}
	}

	/**
	 * Check if the directory exists and is writable
	 * @param dir directory to check
	 * @return true if the directory exists
	 */
	protected boolean dirExists(String dir) {
		Path path = Paths.get(dir);

		try {
			if (Files.isDirectory(path) && Files.isWritable(path)) {
				return true;
			}
		} catch (SecurityException e) {
		}
		return false;
	}

	/**
	 * Call the method
	 * @param method the method to call
	 * @return server response
	 */
	protected IEntity callServerMethod(IMethodEntity method) {
		return callServerMethod(method, null);
	}

	/**
	 * Upload files to the server
	 * @param method the server method to redirect to after uploading
	 * @param files all files to upload
	 * @return server response
	 */
	protected IEntity uploadToServer(IMethodEntity method, List<FieldNameFileWrapper> files) {
		// Get upload url
		GetUploadUrlMethod uploadMethod = new GetUploadUrlMethod();
		uploadMethod.redirectMethod = method.getMethodName().toString();
		IEntity uploadUrlResponse = callServerMethod(uploadMethod);

		// Upload files
		if (uploadUrlResponse instanceof GetUploadUrlResponse) {
			String uploadUrl = ((GetUploadUrlResponse) uploadUrlResponse).uploadUrl;
			if (uploadUrl != null) {
				return callServerMethod(method, uploadUrl, files);
			}
		}

		System.err.println("Failed getting an upload url");
		System.exit(1);
		return null;
	}

	/**
	 * Call the method
	 * @param method the method to call
	 * @param files optional files to upload
	 * @return server response
	 */
	private IEntity callServerMethod(IMethodEntity method, List<FieldNameFileWrapper> files) {
		return callServerMethod(method, mUrl + method.getMethodName().toString(), files);
	}

	/**
	 * Call the method
	 * @param method the method to call
	 * @param url full URL to call on the server
	 * @param files optional files to upload
	 * @return server response
	 */
	private IEntity callServerMethod(IMethodEntity method, String url, List<FieldNameFileWrapper> files) {
		byte[] entitySend = NetworkEntitySerializer.serializeEntity(method);
		if (entitySend != null) {
			try {
				HttpPostBuilder postBuilder = new HttpPostBuilder(url);

				// Add files
				if (files != null && !files.isEmpty()) {
					postBuilder.doFileUpload();
					for (FieldNameFileWrapper fieldNameFile : files) {
						postBuilder.addFile(fieldNameFile.mFieldName, fieldNameFile.mFile);
					}
				}

				// Add method entity as base64
				postBuilder.addParameter(ENTITY_NAME, Base64Coder.encode(entitySend));


				HttpURLConnection connection = postBuilder.build();

				// Get response
				BufferedInputStream bufferedResponse = new BufferedInputStream(connection.getInputStream());
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				int n = -1;
				byte[] buffer = new byte[4096];
				while ((n = bufferedResponse.read(buffer)) != -1) {
					byteArrayOutputStream.write(buffer, 0, n);
				}

				connection.disconnect();

				return NetworkEntitySerializer.deserializeEntity(byteArrayOutputStream.toByteArray());
			} catch (IOException e) {
				System.err.println("Failed with calling " + method.getClass().getSimpleName() + " on server.");
				e.printStackTrace();
				System.exit(1);
			}
		}

		return null;
	}

	/**
	 * Send a download request to the server
	 * @param method name of method to call on the server
	 * @param filePath path to write the downloaded file to
	 * @param date creation date of the file
	 * @return true if file was written successfully, false if an error occurred
	 */
	protected boolean downloadRequest(IMethodEntity method, String filePath, Date date) {
		byte[] entity = NetworkEntitySerializer.serializeEntity(method);

		if (entity != null) {
			try {
				HttpPostBuilder postBuilder = new HttpPostBuilder(mUrl + method.getMethodName());
				postBuilder.doFileUpload();
				postBuilder.addParameter(ENTITY_NAME, entity);

				HttpURLConnection connection = postBuilder.build();

				// Write the response to a file
				BufferedInputStream response = new BufferedInputStream(connection.getInputStream());
				File file = new File(mBackupDir + filePath);
				FileOutputStream fileOutputStream = new FileOutputStream(file);
				BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

				int n = -1;
				byte[] buffer = new byte[4096];
				while ((n = response.read(buffer)) != -1) {
					bufferedOutputStream.write(buffer, 0, n);
				}

				bufferedOutputStream.close();
				response.close();
				connection.disconnect();

				// Update file times
				BasicFileAttributeView attributes = Files.getFileAttributeView(Paths.get(mBackupDir + filePath), BasicFileAttributeView.class);
				FileTime fileTime = FileTime.from(date.getTime(), TimeUnit.MILLISECONDS);
				attributes.setTimes(fileTime, fileTime, fileTime);

				return true;
			} catch (IOException e) {
				System.err.println("Failed with downloading " + method.getClass().getSimpleName() + " on server.");
				e.printStackTrace();
				System.exit(1);
			}
		}

		return false;
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

	/** Backup directory */
	private String mBackupDir = "";
	/** Server URL */
	private String mUrl = "";

	private static final String ENTITY_NAME = "entity";
}
