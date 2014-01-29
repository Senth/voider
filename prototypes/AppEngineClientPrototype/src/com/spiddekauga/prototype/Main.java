package com.spiddekauga.prototype;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


/**
 * Prototype testing
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Main {
	/**
	 * @param args
	 */
	public static void main(String args[]) {
		newUser();
		login();
	}

	/**
	 * Tries to login with an existing user
	 */
	public static void login() {
		CloseableHttpClient httpClient = HttpClients.createDefault();

		try {
			URI uri = new URIBuilder()
			.setScheme("http")
			.setHost(mServer)
			.setPath("/login")
			.setParameter("username", "senth")
			.setParameter("password", "bajs")
			.build();

			HttpGet httpGet = new HttpGet(uri);

			CloseableHttpResponse response = null;
			try {
				response = httpClient.execute(httpGet);
				HttpEntity entity = response.getEntity();

				if (entity != null) {
					InputStream contentStream = entity.getContent();
					try {
						long len = entity.getContentLength();
						if (len != -1 && len < CONTENT_STRING_LENGTH_MAX) {
							System.out.println(EntityUtils.toString(entity));
						}
					} finally {
						contentStream.close();
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (response != null) {
					try {
						response.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Tries to create a new user
	 */
	public static void newUser() {
		CloseableHttpClient httpClient = HttpClients.createDefault();

		try {
			URI uri = new URIBuilder()
			.setScheme("http")
			.setHost(mServer)
			.setPath("/newuser")
			.setParameter("username", "senth")
			.setParameter("password", "bajs")
			.build();

			HttpGet httpGet = new HttpGet(uri);

			CloseableHttpResponse response = null;
			try {
				response = httpClient.execute(httpGet);
				HttpEntity entity = response.getEntity();

				if (entity != null) {
					InputStream contentStream = entity.getContent();
					try {
						long len = entity.getContentLength();
						if (len != -1 && len < CONTENT_STRING_LENGTH_MAX) {
							System.out.println(EntityUtils.toString(entity));
						}
					} finally {
						contentStream.close();
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (response != null) {
					try {
						response.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}



	/** Maximum content string length */
	private final static long CONTENT_STRING_LENGTH_MAX = 2048;
	/** Web app location */
	private static String mServer = "localhost:8888";
}
