package com.spiddekauga.prototype;

import java.util.logging.Logger;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.spiddekauga.voider.Config;


/**
 * Prototype testing
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class PrototypeMain {
	/**
	 * @param args
	 */
	public static void main(String args[]) {
		Gdx.app = new ApplicationStub();
		Gdx.files = new LwjglFiles();
		Config.Debug.JUNIT_TEST = true;

	}


	/** Closable http client used in all prototypes */
	private static CloseableHttpClient mHttpClient = HttpClients.createDefault();
	/** Maximum content string length */
	private final static long CONTENT_STRING_LENGTH_MAX = 2048;
	/** Web app location */
	private final static String HOST = "localhost:8888";

	/** Logger */
	private static Logger mLogger = Logger.getLogger(PrototypeMain.class.getName());
}
