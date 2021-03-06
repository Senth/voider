package com.spiddekauga.voider.backup;

import com.spiddekauga.voider.network.backup.DeleteAllBlobsMethod;
import com.spiddekauga.voider.network.backup.DeleteAllBlobsResponse;
import com.spiddekauga.voider.network.backup.RestoreBlobsMethod;
import com.spiddekauga.voider.network.backup.RestoreBlobsResponse;
import com.spiddekauga.voider.network.entities.IEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Restore actions from the Internet
 */
class RestoreAction extends Action {
private static final Logger mLogger = Logger.getLogger(RestoreAction.class.getSimpleName());
private static final int MAX_BLOBS_PER_UPLOAD = 50;
private Date mDate = null;

@Override
public void execute() {
	if (deleteBlobsOnServer()) {
		restoreBlobsToServer();
		mLogger.info("Blobs restored.");
	} else {
		mLogger.warning("Failed to delete server blobs");
	}
}

@Override
public boolean isAllArgumentsSet() {
	return super.isAllArgumentsSet() && mDate != null;
}

/**
 * Delete all old blobs from the server
 * @return true if successful
 */
private boolean deleteBlobsOnServer() {
	DeleteAllBlobsMethod method = new DeleteAllBlobsMethod();
	mLogger.info("Deleting server blobs");
	IEntity entity = callServerMethod(method);

	return entity instanceof DeleteAllBlobsResponse && ((DeleteAllBlobsResponse) entity).isSuccessful();
}

/**
 * Restore all files in the backup directory to the specified date
 */
private void restoreBlobsToServer() {
	ArrayList<FieldNameFileWrapper> uploadFiles = new ArrayList<>();
	restoreBlobsFromDir(new File(getBackupDir()), uploadFiles);
	checkAndUploadFiles(uploadFiles, true);
}

/**
 * Restore blobs from the specified directory. This method is recursive
 * @param dir the directory to iterate over
 * @param uploadFiles all files to be uploaded
 */
private void restoreBlobsFromDir(File dir, ArrayList<FieldNameFileWrapper> uploadFiles) {
	// For all files in the backup directory
	File[] files = dir.listFiles();
	if (files != null) {
		for (File file : files) {
			// Recursive
			if (file.isDirectory()) {
				restoreBlobsFromDir(file, uploadFiles);
			}
			// A file and it should be restored
			else if (isBeforeRestoreDate(file)) {
				// Only upload resources
				try {
					String uuidString = file.getName().split("_")[0];

					// Check if it's resource. Throws exception otherwise
					UUID.fromString(uuidString);
					mLogger.fine("Added file: " + file.getName());

					uploadFiles.add(new FieldNameFileWrapper(file.getName(), file));
					checkAndUploadFiles(uploadFiles, false);
				} catch (IllegalArgumentException e) {
					mLogger.info("Skipping file: " + file.getName());
				}
			}
		}
	}
}

/**
 * Check if we should upload files and then upload them if that's the case
 * @param files all the files to upload
 * @param forceUpload forces upload of the files to occur even if the buffer isn't full
 */
private void checkAndUploadFiles(ArrayList<FieldNameFileWrapper> files, boolean forceUpload) {
	if (forceUpload || files.size() >= MAX_BLOBS_PER_UPLOAD) {
		RestoreBlobsMethod method = new RestoreBlobsMethod();
		mLogger.info("Uploading " + files.size() + " blobs...");
		IEntity response = uploadToServer(method, files);
		files.clear();

		if (response instanceof RestoreBlobsResponse) {
			RestoreBlobsResponse restoreBlobsResponse = (RestoreBlobsResponse) response;

			if (!restoreBlobsResponse.isSuccessful()) {
				mLogger.severe(restoreBlobsResponse.errorMessage);
				mLogger.severe("Failed to restore blobs. Exiting...");
				System.exit(1);
			}
		}
	}
}

/**
 * Checks if the specified file has date at or before the restore date
 * @param file the file to check
 * @return true if the file has a date at or before the restore date
 */
private boolean isBeforeRestoreDate(File file) {
	BasicFileAttributeView attributesView = Files.getFileAttributeView(Paths.get(file.getAbsolutePath()), BasicFileAttributeView.class);
	BasicFileAttributes attributes;
	try {
		attributes = attributesView.readAttributes();
	} catch (IOException e) {
		mLogger.severe("Failed to read time for file " + file.getAbsolutePath() + ". Exiting...");
		e.printStackTrace();
		System.exit(1);
		return false;
	}

	FileTime fileTime = attributes.creationTime();
	return !mDate.before(new Date(fileTime.toMillis()));
}

/**
 * Set the date we want to restore to
 */
void setDate(Date date) {
	mDate = date;
}
}
