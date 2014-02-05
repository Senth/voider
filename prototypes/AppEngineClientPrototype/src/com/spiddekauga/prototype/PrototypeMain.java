package com.spiddekauga.prototype;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.Date;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.spiddekauga.utils.ObjectCrypter;
import com.spiddekauga.voider.network.EnemyDef;
import com.spiddekauga.voider.network.KryoFactory;


/**
 * Prototype testing
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class PrototypeMain {
	/**
	 * @param args
	 */
	public static void main(String args[]) {
		newUser();
		//				login();
		//		testEnemy();
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

	/**
	 * Upload an enemy
	 */
	private static void testEnemy() {
		String uploadUrl = getUploadUrl();

		if (uploadUrl != null) {
			System.out.print("Upload url: " + uploadUrl);
			uploadEnemy(uploadUrl);
		} else {
			System.out.print("Could not get upload url!");
		}
	}

	/**
	 * Actually upload the enemy
	 * @param uploadUrl the upload url
	 */
	private static void uploadEnemy(String uploadUrl) {
		try {
			Kryo kryo = KryoFactory.createKryo();
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			Output output = new Output(byteArrayOutputStream);
			kryo.writeClassAndObject(output, newEnemy());
			ObjectCrypter objectCrypter = CryptConfig.getCrypter();
			byte[] encrypted = objectCrypter.encrypt(output.toBytes());

			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(uploadUrl);
			MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
			ContentBody contentBody = new FileBody(new File(ENEMY_FILE_CERINA));
			httpPost.setHeader("uploadType", "enemy");
			entityBuilder.addPart("fileKey", contentBody);
			entityBuilder.addBinaryBody("resource", encrypted);
			httpPost.setEntity(entityBuilder.build());

			CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
		} catch (IOException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | ShortBufferException | BadPaddingException e) {
			e.printStackTrace();
		}
	}

	/** File for cerina */
	private static final String ENEMY_FILE_CERINA = "/home/senth/Voider/actors/enemies/6ad14d07-3f9d-4eda-b622-2cd711590d95/LATEST";
	/** File for mist */
	private static final String ENEMY_FILE_MIST = "/Users/senth/Voider/actors/enemies/0d001571-c295-48e6-8a7d-0667e763cb5c/LATEST";

	/**
	 * @return upload url
	 */
	private static String getUploadUrl()  {
		CloseableHttpClient httpClient = HttpClients.createDefault();

		String uploadUrl = null;

		try {
			URI uri = new URIBuilder()
			.setScheme("http")
			.setHost(mServer)
			.setPath("/getuploadurl")
			.addParameter("uploadType", "enemy")
			.build();

			HttpPost httpPost = new HttpPost(uri);


			CloseableHttpResponse response = null;
			try {
				response = httpClient.execute(httpPost);

				Header[] headers = response.getHeaders("uploadUrl");
				for (Header header : headers) {
					if (header.getName().equals("uploadUrl")) {
						uploadUrl = header.getValue();
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return uploadUrl;
	}

	/**
	 * @return a newly created stub enemy
	 */
	private static EnemyDef newEnemy() {
		EnemyDef enemyDef = new EnemyDef();

		enemyDef.creator = "creator";
		enemyDef.date = new Date();
		enemyDef.description = "description";
		enemyDef.externalDependencies.add(UUID.randomUUID());
		enemyDef.internalDependencies.add("INTERNAL_DEP1");
		enemyDef.internalDependencies.add("INTERNAL_DEP2");
		enemyDef.id = UUID.randomUUID();
		enemyDef.maxLife = 101;
		enemyDef.movementSpeed = 10;
		enemyDef.movementType = "movementType";
		enemyDef.name = "name";
		enemyDef.originalCreator = "originalCreator";
		enemyDef.revision = 1337;

		return enemyDef;
	}

	/** Maximum content string length */
	private final static long CONTENT_STRING_LENGTH_MAX = 2048;
	/** Web app location */
	private static String mServer = "localhost:8888";
}
