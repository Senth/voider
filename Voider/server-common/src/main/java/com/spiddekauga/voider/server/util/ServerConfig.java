package com.spiddekauga.voider.server.util;

import java.io.UnsupportedEncodingException;

import javax.mail.internet.InternetAddress;


/**
 * Server configuration
 */
public class ServerConfig {
/** Admin email address */
public static final InternetAddress EMAIL_ADMIN;
/** No-reply email address */
public static final InternetAddress EMAIL_NO_REPLY;
/** Beta information location */
public static final String BETA_INFO_URL = Builds.RELEASE.getUrl() + "download.jsp";
/** Version (changelog) file */
public static final String VERSION_FILE = "assets/txt/changelog.txt";
/** inimum text length when searching for text */
public static final int SEARCH_TEXT_LENGTH_MIN = 3;

static {
	InternetAddress adminEmail = null;
	InternetAddress noReplyEmail = null;
	try {
		adminEmail = new InternetAddress("matteus@voider-game.com", "Voider");
		noReplyEmail = new InternetAddress("no-reply@voider-game.com", "Voider");
	} catch (UnsupportedEncodingException e) {
		e.printStackTrace();
	}
	EMAIL_ADMIN = adminEmail;
	EMAIL_NO_REPLY = noReplyEmail;
}

public static class TokenSizes {
	/** Minimum token size for resources */
	public static final int RESOURCE = 1;
}

/** How many results to send */
public static class FetchSizes {
	/** Number of comments to fetch */
	public static final int COMMENTS = 20;
	/** Number of levels to fetch */
	public static final int LEVELS = 12;
	/** Number of actors to fetch */
	public static final int ACTORS = 20;
	/** Number of tags to get */
	public static final int TAGS = 5;
}

public static class UserInfo {
	/** Minimum name length */
	public static final int NAME_LENGTH_MIN = 3;
	/** Minimum password length */
	public static final int PASSWORD_LENGTH_MIN = 5;
	/** Maximum number of tags per user per resource */
	public static final int TAGS_MAX = 5;
	/** Password reset expires in X hours */
	public static final long PASSWORD_RESET_EXPIRE_HOURS = 24;
}
}
