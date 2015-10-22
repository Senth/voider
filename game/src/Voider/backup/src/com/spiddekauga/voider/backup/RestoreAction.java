package com.spiddekauga.voider.backup;

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

import com.spiddekauga.voider.network.backup.DeleteAllBlobsMethod;
import com.spiddekauga.voider.network.backup.DeleteAllBlobsResponse;
import com.spiddekauga.voider.network.backup.RestoreBlobsMethod;
import com.spiddekauga.voider.network.backup.RestoreBlobsResponse;
import com.spiddekauga.voider.network.entities.IEntity;

/**
 * Restore actions from the Internet
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class RestoreAction extends Action {

	@Override
	public void execute() {
		if (deleteBlobsOnServer()) {
			restoreBlobsToServer();

		}
	}

	/**
	 * Delete all old blobs from the server
	 * @return true if successful
	 */
	private boolean deleteBlobsOnServer() {
		DeleteAllBlobsMethod method = new DeleteAllBlobsMethod();
		IEntity entity = callServerMethod(method);

		if (entity instanceof DeleteAllBlobsResponse) {
			return ((DeleteAllBlobsResponse) entity).isSuccessful();
		} else {
			return false;
		}
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
		String dirName = dir.getName();
		boolean dirIsUuid = false;

		// Check if this is a user resource directory, thus the directory is the UUID and
		// filenames are the revision
		try {
			UUID.fromString(dirName);
			dirIsUuid = true;
		} catch (IllegalArgumentException e) {
			// Does nothing
		}

		// For all files in the backup directory
		for (File file : dir.listFiles()) {
			// Recursive
			if (file.isDirectory()) {
				restoreBlobsFromDir(file, uploadFiles);
			}
			// A file and it should be restored
			else if (isBeforeRestoreDate(file)) {
				String filename;

				// Add directory UUID
				if (dirIsUuid) {
					// Filename should be an Integer
					try {
						Integer.parseInt(file.getName());
						filename = dirName + "_" + file.getName();
					}
					// Not an integer, skip this file
					catch (NumberFormatException e) {
						continue;
					}
				}
				// Filename is UUID
				else {
					// Filename should be a UUID
					try {
						UUID.fromString(file.getName());
						filename = file.getName();
					}
					// Not an UUID, skip
					catch (IllegalArgumentException e) {
						continue;
					}
				}

				uploadFiles.add(new FieldNameFileWrapper(filename, file));

				checkAndUploadFiles(uploadFiles, false);
			}
		}
	}

	/**
	 * Check if we should upload files and then upload them if that's the case
	 * @param files all the files to upload
	 * @param forceUpload forces upload of the files to occur even if the buffer isn't
	 *        full
	 */
	private void checkAndUploadFiles(ArrayList<FieldNameFileWrapper> files, boolean forceUpload) {
		if (forceUpload || files.size() >= MAX_BLOBS_PER_UPLOAD) {
			RestoreBlobsMethod method = new RestoreBlobsMethod();
			mLogger.info("Uploading " + files.size() + " blobs...");
			IEntity response = uploadToServer(method, files);

			if (response instanceof RestoreBlobsResponse) {
				RestoreBlobsResponse restoreBlobsResponse = (RestoreBlobsResponse) response;

				if (!restoreBlobsResponse.isSuccessful()) {
					System.err.println(restoreBlobsResponse.errorMessage);
					System.err.println("Failed to restore blobs. Exiting...");
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
			System.err.println("Failed to read time for file " + file.getAbsolutePath() + ". Exiting...");
			e.printStackTrace();
			System.exit(1);
			return false;
		}

		FileTime fileTime = attributes.creationTime();
		return !mDate.before(new Date(fileTime.toMillis()));
	}

	/**
	 * Set the date we want to restore to
	 * @param date
	 */
	void setDate(Date date) {
		mDate = date;
	}

	@Override
	public boolean isAllArgumentsSet() {
		return super.isAllArgumentsSet() && mDate != null;
	}

	private static final Logger mLogger = Logger.getLogger(RestoreAction.class.getSimpleName());
	private static final int MAX_BLOBS_PER_UPLOAD = 100;
	private Date mDate = null;
}
