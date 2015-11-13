package com.spiddekauga.voider.repo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.net.IDownloadProgressListener;
import com.spiddekauga.net.IOutstreamProgressListener;
import com.spiddekauga.net.IProgressListener;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.NetworkEntitySerializer;
import com.spiddekauga.voider.network.misc.BlobDownloadMethod;
import com.spiddekauga.voider.network.misc.BugReportMethod;
import com.spiddekauga.voider.network.misc.GetUploadUrlMethod;
import com.spiddekauga.voider.network.misc.GetUploadUrlResponse;
import com.spiddekauga.voider.network.resource.ResourceRevisionEntity;
import com.spiddekauga.voider.network.resource.RevisionEntity;
import com.spiddekauga.voider.repo.WebGateway.FieldNameFileWrapper;
import com.spiddekauga.voider.repo.resource.ResourceLocalRepo;
import com.spiddekauga.voider.repo.user.User;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.scene.SceneSwitcher;

/**
 * Common class for all Web Repositories
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class WebRepo {
	/**
	 * Creates a new thread that will send and receive a HTTP request
	 * @param methodEntity the entity to send to the server
	 * @param responseListeners class that invoked the WebRepo
	 */
	protected void sendInNewThread(IMethodEntity methodEntity, IResponseListener... responseListeners) {
		sendInNewThread(methodEntity, null, null, responseListeners);
	}

	/**
	 * Creates a new thread that will send and receive a HTTP request
	 * @param methodEntity the entity to send to the server
	 * @param progressListener send upload progress to this listener
	 * @param responseListeners class that invoked the WebRepo
	 */
	protected void sendInNewThread(IMethodEntity methodEntity, IProgressListener progressListener, IResponseListener... responseListeners) {
		sendInNewThread(methodEntity, null, progressListener, responseListeners);
	}

	/**
	 * Creates a new thread that will send and receive a HTTP request
	 * @param methodEntity the entity to send to the server
	 * @param files all the files to upload
	 * @param responseListeners class that invoked the WebRepo
	 */
	protected void sendInNewThread(IMethodEntity methodEntity, ArrayList<FieldNameFileWrapper> files, IResponseListener... responseListeners) {
		sendInNewThread(methodEntity, files, null, responseListeners);
	}

	/**
	 * Creates a new thread that will send and receive a HTTP request
	 * @param methodEntity the entity to send to the server
	 * @param files all the files to upload * @param progressListener send upload progress
	 *        to this listener
	 * @param progressListener send upload progress to this listener
	 * @param responseListeners class that invoked the WebRepo
	 */
	protected void sendInNewThread(IMethodEntity methodEntity, ArrayList<FieldNameFileWrapper> files, IProgressListener progressListener,
			IResponseListener... responseListeners) {
		Thread thread = new ThreadSend(responseListeners, this, methodEntity, files, progressListener);
		thread.start();
	}

	/**
	 * Handle the response from a thread
	 * @param methodEntity the method that was called
	 * @param response the response from the thread
	 * @param responseListeners class that invoked the command
	 */
	protected abstract void handleResponse(IMethodEntity methodEntity, IEntity response, IResponseListener[] responseListeners);

	/**
	 * Serializes and sends the entity
	 * @param methodEntity the entity to send
	 * @param progressListener send upload progress to this listener
	 * @return response entity, null if something went wrong
	 */
	protected static IEntity serializeAndSend(IMethodEntity methodEntity, IOutstreamProgressListener progressListener) {
		byte[] entitySend = NetworkEntitySerializer.serializeEntity(methodEntity);
		if (entitySend != null) {
			byte[] response = WebGateway.sendRequest(methodEntity.getMethodName().toString(), entitySend);
			if (response != null) {
				return NetworkEntitySerializer.deserializeEntity(response);
			}
		}

		return null;
	}

	/**
	 * Send all methods that should upload files via this method
	 * @param method the method that should "called" on the server when the upload is
	 *        finished
	 * @param progressListener send upload progress to this listener
	 * @param files all files that should be uploaded
	 * @return server method response, null if something went wrong
	 */
	protected static IEntity serializeAndSend(IMethodEntity method, IOutstreamProgressListener progressListener, ArrayList<FieldNameFileWrapper> files) {
		// Get upload URL
		GetUploadUrlMethod uploadMethod = new GetUploadUrlMethod();
		uploadMethod.redirectMethod = method.getMethodName().toString();
		byte[] uploadBytes = NetworkEntitySerializer.serializeEntity(uploadMethod);

		byte[] uploadResponseBytes = WebGateway.sendRequest(uploadMethod.getMethodName().toString(), uploadBytes);
		IEntity uploadResponse = NetworkEntitySerializer.deserializeEntity(uploadResponseBytes);


		// Upload files
		if (uploadResponse instanceof GetUploadUrlResponse) {
			String uploadUrl = ((GetUploadUrlResponse) uploadResponse).uploadUrl;
			Gdx.app.debug("WebRepo", "Upload URL: " + uploadUrl);
			if (uploadUrl != null) {
				// Fix upload URL
				int portIndex = uploadUrl.indexOf(":8888");
				if (portIndex > 0) {
					uploadUrl = Config.Network.SERVER_HOST + uploadUrl.substring(portIndex + 6);
					Gdx.app.debug("WebRepo", "Fixed URL: " + uploadUrl);
				}

				byte[] methodBytes = NetworkEntitySerializer.serializeEntity(method);
				byte[] responseBytes = WebGateway.sendRequest(uploadUrl, methodBytes, files, progressListener);
				return NetworkEntitySerializer.deserializeEntity(responseBytes);
			}
		}

		return null;
	}

	/**
	 * Serializes and sends the method. Used for downloading files from the server
	 * @param method the method that should be "called" on the server
	 * @param filePath path to write the file
	 * @return true if file was written successfully, false if an error occurred.
	 */
	protected static boolean serializeAndDownload(IMethodEntity method, String filePath) {
		if (method instanceof BlobDownloadMethod) {
			mLogger.info("Download blob: " + ((BlobDownloadMethod) method).blobKey);
		}
		byte[] methodBytes = NetworkEntitySerializer.serializeEntity(method);

		IEntity entity = NetworkEntitySerializer.deserializeEntity(methodBytes);
		if (entity instanceof BlobDownloadMethod) {
			mLogger.info("Can deserialize");
		} else {
			mLogger.info("Cannot deserialize");
		}

		return WebGateway.downloadRequest(method.getMethodName().toString(), methodBytes, filePath);
	}

	/**
	 * Sets field names and files to upload from various resource revisions
	 * @param resources all resources to upload
	 * @return list with all field names and files to upload
	 */
	protected static ArrayList<FieldNameFileWrapper> createFieldNameFiles(HashMap<UUID, ResourceRevisionEntity> resources) {
		ArrayList<FieldNameFileWrapper> files = new ArrayList<>();

		for (Entry<UUID, ResourceRevisionEntity> entry : resources.entrySet()) {
			ResourceRevisionEntity entity = entry.getValue();
			String resourceName = entity.resourceId.toString();

			// Create files for all revisions
			for (RevisionEntity revisionEntity : entity.revisions) {
				String filepath = Gdx.files.getExternalStoragePath();
				filepath += ResourceLocalRepo.getRevisionFilepath(entity.resourceId, revisionEntity.revision);
				File file = new File(filepath);

				if (file.exists()) {
					files.add(new FieldNameFileWrapper(resourceName + "_" + revisionEntity.revision, file));
				} else {
					Gdx.app.error("WebRepo", "File does not exist: " + filepath);
				}
			}
		}

		return files;
	}

	/**
	 * Sets field names and files to upload
	 * @param resources all the resources to upload
	 * @return list with all field names and files to upload
	 */
	protected static ArrayList<FieldNameFileWrapper> createFieldNameFiles(ArrayList<IResource> resources) {
		ArrayList<FieldNameFileWrapper> files = new ArrayList<>();

		for (IResource resource : resources) {
			// Get file
			String filepath = Gdx.files.getExternalStoragePath();
			filepath += ResourceLocalRepo.getFilepath(resource);
			File file = new File(filepath);

			if (file.exists()) {
				files.add(new FieldNameFileWrapper(resource.getId().toString(), file));
			} else {
				Gdx.app.error("ResourceWebRepo", "File does not exist: " + filepath);
			}
		}

		return files;
	}

	/**
	 * Send responses to caller listeners
	 * @param methodEntity the method that was called
	 * @param response the response to send
	 * @param responseListeners class that invoked the WebRepeo
	 */
	protected static void sendResponseToListeners(IMethodEntity methodEntity, IEntity response, IResponseListener[] responseListeners) {
		if (response != null) {
			for (IResponseListener responseListener : responseListeners) {
				responseListener.handleWebResponse(methodEntity, response);
			}
		}
	}

	/**
	 * Download blobs in new threads
	 * @param blobs all blobs to download
	 * @param progressListener download progress listener
	 */
	protected void downloadInThreads(ArrayList<? extends DownloadBlobWrapper> blobs, IDownloadProgressListener progressListener) {
		if (progressListener != null) {
			progressListener.handleFileDownloaded(0, blobs.size());
		}

		LinkedList<DownloadBlobWrapper> blobsLeft = new LinkedList<>();
		blobsLeft.addAll(blobs);
		ThreadDownload.addBlobsToDownload(blobs);
		ThreadDownload.createThreads();

		// Wait to return until all blobs have been downloaded or failed
		while (!blobsLeft.isEmpty()) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			boolean removedSome = false;
			Iterator<DownloadBlobWrapper> it = blobsLeft.iterator();
			while (it.hasNext()) {
				DownloadBlobWrapper entity = it.next();
				if (!entity.isDownloading()) {
					it.remove();
					removedSome = true;
				}
			}

			if (removedSome && progressListener != null) {
				progressListener.handleFileDownloaded(blobs.size() - blobsLeft.size(), blobs.size());
			}
		}
	}

	/**
	 * Wrapper class for downloads
	 */
	protected class DownloadBlobWrapper {
		/**
		 * @param method
		 * @param filepath
		 */
		protected DownloadBlobWrapper(BlobDownloadMethod method, String filepath) {
			mMethod = method;
			mFilepath = filepath;
		}

		/**
		 * @return true if downloaded
		 */
		protected synchronized boolean isDownloaded() {
			return mDownloaded != null && mDownloaded;
		}

		/**
		 * @return true if failed to download
		 */
		protected synchronized boolean isFailed() {
			return mDownloaded != null && !mDownloaded;
		}

		/**
		 * @return true if this blob hasn't been downloaded yet
		 */
		protected synchronized boolean isDownloading() {
			return mDownloaded == null;
		}

		/**
		 * Set download state
		 * @param downloaded true if downloaded, false if failed.
		 */
		protected synchronized void setDownloaded(boolean downloaded) {
			mDownloaded = downloaded;
		}

		private String mFilepath;
		private BlobDownloadMethod mMethod;
		private Boolean mDownloaded = null;
	}

	/**
	 * Wrapper thread for downloading blobs from the server
	 */
	private static class ThreadDownload extends Thread {

		/**
		 * Creates new threads if necessary
		 */
		static void createThreads() {
			int threadsToCreate = mToDownload.size();

			if (threadsToCreate > Config.Network.CONNECTIONS_MAX) {
				threadsToCreate = Config.Network.CONNECTIONS_MAX;
			}

			threadsToCreate -= mThreads.size();


			for (int i = 0; i < threadsToCreate; i++) {
				new ThreadDownload().start();
			}
		}

		@Override
		public void run() {
			while (!mToDownload.isEmpty()) {
				try {
					DownloadBlobWrapper toDownload = mToDownload.take();

					boolean success = serializeAndDownload(toDownload.mMethod, toDownload.mFilepath);
					toDownload.setDownloaded(success);

				} catch (IndexOutOfBoundsException | InterruptedException e) {
					// Do nothing
				}
			}
		}

		/**
		 * Add all blobs to download
		 * @param blobs all blobs to download
		 */
		public static void addBlobsToDownload(List<? extends DownloadBlobWrapper> blobs) {
			for (DownloadBlobWrapper downloadBlobWrapper : blobs) {
				try {
					mToDownload.put(downloadBlobWrapper);
				} catch (InterruptedException e) {
					// Do nothing
				}
			}
		}

		/** All things to download */
		static BlockingQueue<DownloadBlobWrapper> mToDownload = new LinkedBlockingQueue<DownloadBlobWrapper>();
		/** All active threads */
		static BlockingQueue<ThreadDownload> mThreads = new LinkedBlockingQueue<ThreadDownload>();
	}

	/**
	 * Wrapper class for sending information to the server (including uploads)
	 */
	private static class ThreadSend extends Thread {
		/**
		 * Constructs a web thread
		 * @param responseListeners class that invoked the WebRepeo
		 * @param webRepo the web repository to send the response to
		 * @param methodEntity the method to send
		 * @param files all the files to send, set to null to not send any files
		 * @param progressListener send upload progress to this listener
		 */
		ThreadSend(IResponseListener[] responseListeners, WebRepo webRepo, IMethodEntity methodEntity, ArrayList<FieldNameFileWrapper> files,
				IProgressListener progressListener) {
			mMethodEntity = methodEntity;
			mWebRepo = webRepo;
			mResponseListeners = responseListeners;
			mFiles = files;
			mProgressListener = progressListener;
		}

		@Override
		public void run() {
			try {
				IEntity response = null;

				IOutstreamProgressListener progressListener = null;
				if (mProgressListener instanceof IOutstreamProgressListener) {
					progressListener = (IOutstreamProgressListener) mProgressListener;
				}

				if (mFiles == null || mFiles.isEmpty()) {
					response = serializeAndSend(mMethodEntity, progressListener);
				} else {
					response = serializeAndSend(mMethodEntity, progressListener, mFiles);
				}

				mWebRepo.handleResponse(mMethodEntity, response, mResponseListeners);
			} catch (RuntimeException e) {
				if (!(mMethodEntity instanceof BugReportMethod) && User.getGlobalUser().isLoggedIn()) {
					SceneSwitcher.handleException(e);
				} else {
					throw e;
				}
			}
		}

		/** The method to call on the server */
		IMethodEntity mMethodEntity;
		/** Files to send, null if not used */
		ArrayList<FieldNameFileWrapper> mFiles;
		/** The web repository to send the response to */
		WebRepo mWebRepo;
		/** Caller instance, i.e. the class that invoked the WebRepo */
		IResponseListener[] mResponseListeners;
		/** Progress listener */
		IProgressListener mProgressListener;
	}

	private static final Logger mLogger = Logger.getLogger(WebRepo.class.getSimpleName());
}
