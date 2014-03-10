package com.spiddekauga.voider.repo;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.network.entities.method.NetworkEntitySerializer;

/**
 * Common class for all Web Repositories
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
abstract class WebRepo {
	/**
	 * Creates a new thread that will send and receive a HTTP request
	 * @param methodEntity the entity to send to the server
	 * @param callerResponseListener class that invoked the WebRepo
	 */
	protected void sendInNewThread(IMethodEntity methodEntity, ICallerResponseListener callerResponseListener) {
		Thread thread = new ThreadWrapper(methodEntity, this, callerResponseListener);
		thread.start();
	}

	/**
	 * Handle the response from a thread
	 * @param methodEntity the method that was called
	 * @param response the response from the thread
	 * @param callerResponseListener class that invoked the command
	 */
	protected abstract void handleResponse(IMethodEntity methodEntity, IEntity response, ICallerResponseListener callerResponseListener);

	/**
	 * Serializes and sends the entity
	 * @param methodEntity the entity to send
	 * @return response entity, null if something went wrong
	 */
	protected static IEntity serializeAndSend(IMethodEntity methodEntity) {
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
	 * Wrapper class for all threads
	 */
	private static class ThreadWrapper extends Thread {
		/**
		 * Constructs a web thread
		 * @param methodEntity the method to send
		 * @param webRepo the web repository to send the response to
		 * @param callerResponseListener class that invoked the WebRepeo
		 */
		ThreadWrapper(IMethodEntity methodEntity, WebRepo webRepo, ICallerResponseListener callerResponseListener) {
			mMethodEntity = methodEntity;
			mWebRepo = webRepo;
			mCallerRepsonseListener = callerResponseListener;
		}

		@Override
		public void run() {
			IEntity response = serializeAndSend(mMethodEntity);

			mWebRepo.handleResponse(mMethodEntity, response, mCallerRepsonseListener);
		}

		/** The method to call on the server */
		IMethodEntity mMethodEntity;
		/** The web repository to send the response to */
		WebRepo mWebRepo;
		/** Caller instance, i.e. the class that invoked the WebRepo */
		ICallerResponseListener mCallerRepsonseListener;
	}
}
