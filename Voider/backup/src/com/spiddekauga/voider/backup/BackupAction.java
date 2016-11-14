package com.spiddekauga.voider.backup;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

import com.spiddekauga.voider.network.backup.BackupNewBlobsMethod;
import com.spiddekauga.voider.network.backup.BackupNewBlobsResponse;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.misc.BlobDownloadMethod;
import com.spiddekauga.voider.network.resource.ResourceBlobEntity;
import com.spiddekauga.voider.network.resource.ResourceRevisionBlobEntity;

/**
 * Backups the blobs from Internet

 */
public class BackupAction extends Action {
	@Override
	public void execute() {
		BackupNewBlobsResponse response = getNewBlobs();

		if (response != null && response.isSuccessful()) {
			downloadNewBlobs(response);
			updateBackupDate();
		} else {
			mLogger.severe("Couldn't connect properly to server");
		}
	}

	/**
	 * Checks with the server for new blobs
	 * @return new blobs to be downloaded
	 */
	private BackupNewBlobsResponse getNewBlobs() {
		mLogger.info("Fetching info about new blobs from server");
		BackupNewBlobsMethod method = new BackupNewBlobsMethod();
		method.lastBackup = getLastBackupDate();
		IEntity entity = callServerMethod(method);

		if (entity instanceof BackupNewBlobsResponse) {
			return (BackupNewBlobsResponse) entity;
		} else {
			return null;
		}
	}

	/**
	 * Download new blobs
	 * @param newBlobs all new blobs to download
	 */
	private void downloadNewBlobs(BackupNewBlobsResponse newBlobs) {
		BlobDownloadMethod downloadMethod = new BlobDownloadMethod();

		// Published
		for (ResourceBlobEntity publishedBlob : newBlobs.publishedBlobs) {
			mLogger.info("Downloading published blob " + publishedBlob.resourceId);
			downloadMethod.blobKey = publishedBlob.blobKey;
			String filepath = publishedBlob.resourceId.toString();
			publishedBlob.downloaded = downloadRequest(downloadMethod, filepath, publishedBlob.created);
		}

		// User Resources
		for (ResourceRevisionBlobEntity userBlob : newBlobs.userBlobs) {
			downloadMethod.blobKey = userBlob.blobKey;

			userBlob.downloaded = createRevisionDirectory(userBlob.resourceId);
			if (userBlob.downloaded) {
				mLogger.info("Downloading user blob " + userBlob.resourceId + ":" + userBlob.revision);
				String filepath = "userResources/" + userBlob.resourceId + "/" + userBlob.resourceId + "_" + userBlob.revision;
				userBlob.downloaded = downloadRequest(downloadMethod, filepath, userBlob.created);
			}
		}
	}

	/**
	 * Create resource directory for revisions
	 * @param resourceId id of the resource to create a directory for
	 * @return true if the resource directory has been created or already exists
	 */
	private boolean createRevisionDirectory(UUID resourceId) {
		Path path = Paths.get(getBackupRevDir() + resourceId.toString());
		boolean createDir = false;
		if (Files.exists(path)) {
			// Is a directory
			if (!Files.isDirectory(path)) {
				try {
					Files.delete(path);
					createDir = true;
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}
		} else {
			createDir = true;
		}

		if (createDir) {
			try {
				Files.createDirectories(path);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}


		return true;
	}

	/**
	 * @return last backup date. If first backup it returns 1970-01-01 (or Date(0))
	 */
	private Date getLastBackupDate() {
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(getBackupDir() + DATE_FILE);
		} catch (FileNotFoundException e) {
			return new Date(0);
		}

		DataInputStream dataInputStream = new DataInputStream(fileInputStream);
		try {
			long time = dataInputStream.readLong();
			dataInputStream.close();
			return new Date(time);
		} catch (IOException e) {
			mLogger.severe("Error reading last backup date");
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}

	/**
	 * Set last backup date to now
	 */
	private void updateBackupDate() {
		Date now = new Date();

		try {
			FileOutputStream fileOutputStream = new FileOutputStream(getBackupDir() + DATE_FILE);
			DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
			dataOutputStream.writeLong(now.getTime());
			dataOutputStream.close();
		} catch (IOException | SecurityException e) {
			mLogger.severe("Error writing last backup date");
			e.printStackTrace();
		}
	}

	private static final Logger mLogger = Logger.getLogger(BackupAction.class.getSimpleName());
	private static final String DATE_FILE = "LAST_BACKUP";
}
