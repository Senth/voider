package com.spiddekauga.voider.server.util;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.mail.internet.InternetAddress;

import com.google.appengine.api.utils.SystemProperty;


/**
 * Server configuration
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ServerConfig {
	/** Database tables */
	@SuppressWarnings("javadoc")
	public static class DatastoreTables {
		/** Blob information for all blobs */
		public static final String USERS = "users";
		public static final String PASSWORD_RESET = "password_reset";
		public static final String MOTD = "motd";
		public static final String RESTORE_DATE = "restore_date";
		public static final String PUBLISHED = "published";
		/** Published dependencies */
		public static final String DEPENDENCY = "dependency";
		public static final String USER_LEVEL_STAT = "user_level_stat";
		public static final String LEVEL_STAT = "level_stat";
		public static final String LEVEL_TAG = "level_tag";
		public static final String RESOURCE_COMMENT = "resource_comment";
		public static final String ACTOR_STAT = "actor_stat";
		public static final String SYNC_PUBLISHED = "sync_published";
		public static final String USER_RESOURCES = "user_resources";
		public static final String USER_RESOURCES_DELETED = "user_resources_deleted";
		public static final String HIGHSCORE = "highscore";
		// Analytics
		public static final String ANALYTICS_SESSION = "analytics_session";
		public static final String ANALYTICS_SCENE = "analytics_scene";
		public static final String ANALYTICS_EVENT = "analytics_event";
		// Beta sign-up
		public static final String BETA_KEY = "beta_key";
		public static final String BETA_SIGNUP = "beta_signup";
		public static final String BETA_GROUP = "beta_group";
		public static final String MAINTENANCE = "maintenance";

		// Backup
		public static final String BACKUP_INFO = "_AE_Backup_Information_Entities";

		// -- Columns --
		// Users
		public static class CUsers {
			public static final String USERNAME = "username";
			public static final String USERNAME_LOWCASE = "username_lowcase";
			public static final String EMAIL = "email";
			public static final String PASSWORD = "password";
			public static final String CREATED = "created";
			public static final String LOGGED_IN = "logged-in";
			public static final String PRIVATE_KEY = "private_key";
		}

		// Reset Password Tokens
		public static class CPasswordReset {
			public static final String TOKEN = "token";
			public static final String EXPIRES = "expires";
		}

		// Message of the Day
		public static class CMotd {
			public static final String TITLE = "title";
			public static final String CONTENT = "content";
			public static final String TYPE = "type";
			public static final String CREATED = "created";
			public static final String EXPIRES = "expires";
		}

		// Restore Date
		public static class CRestoreDate {
			public static final String FROM_DATE = "from_date";
			public static final String TO_DATE = "to_date";
		}

		// Published
		public static class CPublished {
			public static final String TYPE = "type";
			public static final String NAME = "name";
			public static final String ORIGINAL_CREATOR_KEY = "original_creator_key";
			public static final String DESCRIPTION = "description";
			/** Published date */
			public static final String DATE = "date";
			public static final String BLOB_KEY = "blob_key";
			public static final String PNG = "png";
			public static final String COPY_PARENT_ID = "copy_parent_id";
			public static final String RESOURCE_ID = "resource_id";

			// Level specific
			public static final String LEVEL_BLOB_KEY = "level_blob_key";
			public static final String LEVEL_ID = "level_id";
		}

		// Dependency
		public static class CDependency {
			public static final String DEPENDENCY = "dependency";
		}

		// User Level Stat
		public static class CUserLevelStat {
			public static final String LEVEL_KEY = "level_key";
			public static final String LAST_PLAYED = "last_played";
			public static final String RATING = "rating";
			public static final String PLAY_COUNT = "play_count";
			public static final String DEATH_COUNT = "death_count";
			public static final String CLEAR_COUNT = "clear_count";
			public static final String TAGS = "tags";
			public static final String BOOKMARK = "bookmark";
			public static final String UPDATED = "updated";
		}

		// Level stat
		public static class CLevelStat {
			public static final String PLAY_COUNT = "play_count";
			public static final String RATING_SUM = "rating_sum";
			/** Rating count */
			public static final String RATINGS = "ratings";
			public static final String RATING_AVG = "rating_avg";
			public static final String CLEAR_COUNT = "clear_count";
			public static final String BOOKMARS = "bookmarks";
			public static final String DEATH_COUNT = "death_count";
		}

		// Level tag
		public static class CLevelTag {
			public static final String TAG = "tag";
			public static final String COUNT = "count";
		}

		// Resource comment
		public static class CResourceComment {
			public static final String USERNAME = "username";
			public static final String COMMENT = "comment";
			public static final String DATE = "date";
		}

		// Sync published
		public static class CSyncPublished {
			public static final String PUBLISHED_KEY = "published_key";
			public static final String DOWNLOAD_DATE = "download_date";
		}

		// User resources
		public static class CUserResources {
			public static final String RESOURCE_ID = "resource_id";
			public static final String REVISION = "revision";
			public static final String TYPE = "type";
			public static final String CREATED = "created";
			public static final String UPLOADED = "uploaded";
			public static final String BLOB_KEY = "blob_key";
		}

		// User resources deleted
		public static class CUserResourcesDeleted {
			public static final String RESOURCE_ID = "resource_id";
			public static final String DATE = "date";
		}

		// Highscore
		public static class CHighscore {
			public static final String USERNAME = "username";
			public static final String SCORE = "score";
			public static final String CREATED = "created";
			public static final String UPLOADED = "uploaded";
			public static final String LEVEL_ID = "level_id";
		}

		// Analytics (common attributes)
		public static class CAnalytics {
			public static final String EXPORTED = "exported";
		}

		// Analytics session
		public static class CAnalyticsSession extends CAnalytics {
			public static final String START_TIME = "start_time";
			public static final String LENGTH = "length";
			public static final String USER_ANALYTICS_ID = "user_analytics_id";
			public static final String PLATFORM = "platform";
			public static final String OS = "os";
			public static final String SCREEN_SIZE = "screen_size";
		}

		// Analytics scene
		public static class CAnalyticsScene extends CAnalytics {
			public static final String START_TIME = "start_time";
			public static final String LENGTH = "length";
			public static final String NAME = "name";
			public static final String LOAD_TIME = "load_time";
			public static final String DROPOUT = "dropout";
		}

		// Analytics event
		public static class CAnalyticsEvent extends CAnalytics {
			public static final String TIME = "time";
			public static final String NAME = "name";
			public static final String DATA = "data";
			public static final String TYPE = "type";
		}

		// Beta keys
		public static class CBetaKey {
			public static final String KEY = "key";
			public static final String USED = "used";
		}

		// Beta sign-up
		public static class CBetaSignUp {
			public static final String EMAIL = "email";
			public static final String BETA_KEY = "beta_key";
			public static final String DATE = "date";
			public static final String CONFIRM_EXPIRES = "confirm_expires";
			public static final String CONFIRM_KEY = "confirm_key";
		}

		// Beta group
		public static class CBetaGroup {
			public static final String NAME = "name";
			public static final String HASH = "hash";
		}

		// Maintenance
		public static class CMaintenance {
			public static final String MODE = "mode";
			public static final String REASON = "reason";
			public static final String MOTD_KEY = "motd_key";
		}

		// Backup Information
		public static class CBackupInfo {
			public static final String ACTIVE_JOBS = "active_jobs";
			public static final String COMPLETE_TIME = "complete_time";
			public static final String COMPLETED_JOBS = "completed_jobs";
			public static final String FILESYSTEM = "filesystem";
			public static final String GS_HANDLE = "gs_handle";
			public static final String KINDS = "kinds";
			public static final String NAME = "name";
			public static final String START_TIME = "start_time";
		}

		/**
		 * Private constructor to enforce singleton usage
		 */
		private DatastoreTables() {
		}
	}

	@SuppressWarnings("javadoc")
	public static class SearchTables {
		public static final String ENEMY = "enemy_def";
		public static final String BULLET = "bullet_def";
		public static final String LEVEL = "level_def";

		// Common for all resources
		public static class SDef {
			public static final String CREATOR = "creator";
			public static final String ORIGINAL_CREATOR = "original_creator";
			public static final String NAME = "name";
			public static final String DATE = "date";
		}

		// Enemy
		public static class SEnemy extends SDef {
			public static final String MOVEMENT_TYPE = "movement_type";
			public static final String MOVEMENT_SPEED = "movement_speed";
			public static final String MOVEMENT_SPEED_CAT = "movement_speed_cat";
			public static final String HAS_WEAPON = "has_weapon";
			public static final String BULLET_SPEED = "bullet_speed";
			public static final String BULLET_SPEED_CAT = "bullet_speed_cat";
			public static final String AIM_TYPE = "aim_type";
			public static final String BULLET_DAMAGE = "bullet_damage";
			public static final String BULLET_DAMAGE_CAT = "bullet_damage_cat";
			public static final String DESTROY_ON_COLLIDE = "destroy_on_collide";
			public static final String COLLISION_DAMAGE = "collision_damage";
			public static final String COLLISION_DAMAGE_CAT = "collision_damage_cat";
		}

		// Bullet
		public static class SBullet extends SDef {

		}

		// Level
		public static class SLevel extends SDef {
			public static final String LEVEL_LENGTH = "level_length";
			public static final String LEVEL_LENGTH_CAT = "level_length_cat";
			public static final String LEVEL_SPEED = "level_speed";
			public static final String LEVEL_SPEED_CAT = "level_speed_cat";
			public static final String TAGS = "tags";
		}
	}

	/** Admin email address */
	public static final InternetAddress EMAIL_ADMIN;
	/** No-reply email address */
	public static final InternetAddress EMAIL_NO_REPLY;
	/** Beta information location */
	public static final String BETA_INFO_URL = Builds.RELEASE.getUrl() + "beta.jsp";

	/**
	 * All different servers builds
	 */
	public enum Builds {
		/** Development server */
		DEV("voider-dev", "https://voider-dev.appspot.com/", null, "Voider-beta"),
		/** Nightly server */
		NIGHTLY("voider-nightly", "https://voider-nightly.appspot.com/", null, null),
		/** Beta server */
		BETA("voider-beta", "https://voider-beta.appspot.com/", null, "Voider-beta"),
		/** Release server */
		RELEASE("voider-thegame", "http://voider-game.com/", "https://voider-thegame.appspot.com/", null),

		;

		/**
		 * @param appId application id of the build
		 * @param url URL for this app
		 * @param appspotUrl appspot internal URL used for this app, set to null if same
		 *        as url
		 * @param downloadName for downloading stuff, null to not use
		 */
		private Builds(String appId, String url, String appspotUrl, String downloadName) {
			mAppId = appId;
			mUrl = url;
			mDownloadName = downloadName;

			if (appspotUrl == null) {
				mAppspotUrl = url;
			} else {
				mAppspotUrl = appspotUrl;
			}
		}

		/**
		 * @return true if this is the current server
		 */
		public boolean isCurrent() {
			return SystemProperty.applicationId.get().equals(mAppId);
		}

		/**
		 * @return download URL for desktop client
		 */
		public String getDownloadDesktopUrl() {
			if (mDownloadName != null) {
				return DOWNLOAD_URL_PREFIX + mDownloadName + DESKTOP_SUFFIX;
			} else {
				return null;
			}
		}

		/**
		 * @return URL for this app
		 */
		public String getUrl() {
			return mUrl;
		}

		/**
		 * @return Appspot internal URL for this app
		 */
		public String getAppspotUrl() {
			return mAppspotUrl;
		}

		/**
		 * @return get the current build, null if none was found
		 */
		public static Builds getCurrent() {
			for (Builds build : Builds.values()) {
				if (build.isCurrent()) {
					return build;
				}
			}
			return null;
		}

		private String mAppId;
		private String mUrl;
		private String mAppspotUrl;
		private String mDownloadName;

		private static final String DOWNLOAD_URL_PREFIX = "http://storage.googleapis.com/voider-shared/app/";
		private static final String DESKTOP_SUFFIX = ".jar";
	}

	/**
	 * All maintenance modes
	 */
	public enum MaintenanceModes {
		/** Up and running */
		UP,
		/** Server is down for maintenance */
		DOWN,

		;

		/**
		 * Convert a string back to a maintenance mode
		 * @param mode
		 * @return the enumeration of mode, null if not found
		 */
		public static MaintenanceModes fromString(String mode) {
			return mStringToEnum.get(mode);
		}

		private static Map<String, MaintenanceModes> mStringToEnum = new HashMap<>();

		static {
			for (MaintenanceModes mode : MaintenanceModes.values()) {
				mStringToEnum.put(mode.toString(), mode);
			}
		}
	}

	/** Public Search tokenize sizes */
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

	/** Minimum text length when searching for text */
	public static final int SEARCH_TEXT_LENGTH_MIN = 3;

	/** User information */
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

	// Initialization of some variables
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
}
