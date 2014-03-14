package com.spiddekauga.voider.repo;

import java.io.File;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.utils.IOutstreamProgressListener;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.method.GetUploadUrlMethod;
import com.spiddekauga.voider.network.entities.method.GetUploadUrlMethodResponse;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.network.entities.method.NetworkEntitySerializer;
import com.spiddekauga.voider.repo.WebGateway.FieldNameFileWrapper;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.Pools;

/**
 * Common class for all Web Repositories
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
abstract class WebRepo {
	/**
	 * Creates a new thread that will send and receive a HTTP request
	 * @param methodEntity the entity to send to the server
	 * @param callerResponseListeners class that invoked the WebRepo
	 */
	protected void sendInNewThread(IMethodEntity methodEntity, ICallerResponseListener... callerResponseListeners) {
		sendInNewThread(methodEntity, null, null, callerResponseListeners);
	}

	/**
	 * Creates a new thread that will send and receive a HTTP request
	 * @param methodEntity the entity to send to the server
	 * @param progressListener send upload progress to this listener
	 * @param callerResponseListeners class that invoked the WebRepo
	 */
	protected void sendInNewThread(IMethodEntity methodEntity, IOutstreamProgressListener progressListener, ICallerResponseListener... callerResponseListeners) {
		sendInNewThread(methodEntity, null, progressListener, callerResponseListeners);
	}

	/**
	 * Creates a new thread that will send and receive a HTTP request
	 * @param methodEntity the entity to send to the server
	 * @param files all the files to upload
	 * @param callerResponseListeners class that invoked the WebRepo
	 */
	protected void sendInNewThread(IMethodEntity methodEntity, ArrayList<FieldNameFileWrapper> files, ICallerResponseListener... callerResponseListeners) {
		sendInNewThread(methodEntity, files, null, callerResponseListeners);
	}

	/**
	 * Creates a new thread that will send and receive a HTTP request
	 * @param methodEntity the entity to send to the server
	 * @param files all the files to upload
	 * 	 * @param progressListener send upload progress to this listener
	 * @param callerResponseListeners class that invoked the WebRepo
	 */
	protected void sendInNewThread(IMethodEntity methodEntity, ArrayList<FieldNameFileWrapper> files, IOutstreamProgressListener progressListener, ICallerResponseListener... callerResponseListeners) {
		Thread thread = new ThreadWrapper(callerResponseListeners, this, methodEntity, files, progressListener);
		thread.start();
	}

	/**
	 * Handle the response from a thread
	 * @param methodEntity the method that was called
	 * @param response the response from the thread
	 * @param callerResponseListeners class that invoked the command
	 */
	protected abstract void handleResponse(IMethodEntity methodEntity, IEntity response, ICallerResponseListener[] callerResponseListeners);

	/**
	 * Serializes and sends the entity
	 * @param methodEntity the entity to send
	 * @param progressListener send upload progress to this listener
	 * @return response entity, null if something went wrong
	 */
	protected static IEntity serializeAndSend(IMethodEntity methodEntity, IOutstreamProgressListener progressListener) {
		byte[] entitySend = NetworkEntitySerializer.serializeEntity(methodEntity);
		if (entitySend != null) {
			byte[] response = WebGateway.sendRequest(methodEntity.getMethodName(), entitySend);
			if (response != null) {
				return NetworkEntitySerializer.deserializeEntity(response);
			}
		}

		return null;
	}

	/**
	 * Send all methods that should upload files via this method
	 * @param method the method that should "called" on the server
	 * when the upload is finished
	 * @param progressListener send upload progress to this listener
	 * @param files all files that should be uploaded
	 * @return server method response, null if something went wrong
	 */
	protected static IEntity serializeAndSend(IMethodEntity method, IOutstreamProgressListener progressListener, ArrayList<FieldNameFileWrapper> files) {
		// Get upload URL
		GetUploadUrlMethod uploadMethod = new GetUploadUrlMethod();
		uploadMethod.redirectMethod = method.getMethodName();
		byte[] uploadBytes = NetworkEntitySerializer.serializeEntity(uploadMethod);

		byte[] uploadResponseBytes = WebGateway.sendRequest(uploadMethod.getMethodName(), uploadBytes);
		IEntity uploadResponse = NetworkEntitySerializer.deserializeEntity(uploadResponseBytes);


		// Upload files
		if (uploadResponse instanceof GetUploadUrlMethodResponse) {
			String uploadUrl = ((GetUploadUrlMethodResponse) uploadResponse).uploadUrl;
			if (uploadUrl != null) {
				byte[] methodBytes = NetworkEntitySerializer.serializeEntity(method);
				byte[] responseBytes = WebGateway.sendRequest(uploadUrl, methodBytes, files, progressListener);
				return NetworkEntitySerializer.deserializeEntity(responseBytes);
			}
		}

		return null;
	}

	/**
	 * Sets field names and files to upload
	 * @param resources all the resources to upload
	 * @return list with all field names and files to upload
	 */
	protected static ArrayList<FieldNameFileWrapper> createFieldNameFiles(ArrayList<IResource> resources) {
		@SuppressWarnings("unchecked")
		ArrayList<FieldNameFileWrapper> files = Pools.arrayList.obtain();

		for (IResource resource : resources) {
			// Get file
			String filepath = Gdx.files.getExternalStoragePath();
			filepath += ResourceLocalRepo.getFilepath(resource);
			File file = new File(filepath);

			if (file.exists()) {
				FieldNameFileWrapper fieldNameFile = new FieldNameFileWrapper();
				fieldNameFile.fieldName = resource.getId().toString();
				fieldNameFile.file = file;

				files.add(fieldNameFile);
			} else {
				Gdx.app.error("ResourceWebRepo", "File does not exist: " + filepath);
			}
		}

		return files;
	}

	/**
	 * Wrapper class for all threads
	 */
	private static class ThreadWrapper extends Thread {
		/**
		 * Constructs a web thread
		 * @param callerResponseListeners class that invoked the WebRepeo
		 * @param webRepo the web repository to send the response to
		 * @param methodEntity the method to send
		 * @param files all the files to send, set to null to not send any files
		 * @param progressListener send upload progress to this listener
		 */
		ThreadWrapper(ICallerResponseListener[] callerResponseListeners, WebRepo webRepo, IMethodEntity methodEntity, ArrayList<FieldNameFileWrapper> files, IOutstreamProgressListener progressListener) {
			mMethodEntity = methodEntity;
			mWebRepo = webRepo;
			mCallerRepsonseListeners = callerResponseListeners;
			mFiles = files;
			mProgressListener = progressListener;
		}

		@Override
		public void run() {
			try {
				IEntity response = null;
				if (mFiles == null || mFiles.isEmpty()) {
					response = serializeAndSend(mMethodEntity, mProgressListener);
				} else {
					response = serializeAndSend(mMethodEntity, mProgressListener, mFiles);
				}

				mWebRepo.handleResponse(mMethodEntity, response, mCallerRepsonseListeners);
			} catch (RuntimeException e) {
				if (Config.Debug.EXCEPTION_HANDLER) {
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
		ICallerResponseListener[] mCallerRepsonseListeners;
		/** Progress listener */
		IOutstreamProgressListener mProgressListener;
	}
}
