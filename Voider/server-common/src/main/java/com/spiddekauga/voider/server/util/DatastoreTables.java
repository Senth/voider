package com.spiddekauga.voider.server.util;

/** Database tables */
@SuppressWarnings("javadoc")
public class DatastoreTables {
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
	public static final String MAINTENANCE = "maintenance";
	public static final String CONNECTED_USER = "connected_user";
	// Analytics
	public static final String ANALYTICS_SESSION = "analytics_session";
	public static final String ANALYTICS_SCENE = "analytics_scene";
	public static final String ANALYTICS_EVENT = "analytics_event";
	// Beta sign-up
	public static final String BETA_KEY = "beta_key";
	public static final String BETA_SIGNUP = "beta_signup";
	public static final String BETA_GROUP = "beta_group";


	// Backup
	public static final String BACKUP_INFO = "_AE_Backup_Information";

	/**
	 * Private constructor to enforce singleton usage
	 */
	private DatastoreTables() {
	}

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

	// Maintenance
	public static class CMaintenance {
		public static final String MODE = "mode";
		public static final String REASON = "reason";
		public static final String MOTD_KEY = "motd_key";
	}

	// Connected User
	public static class CConnectedUser {
		public static final String CONNECTED_TIME = "connected_time";
		public static final String CHANNEL_ID = "channel_id";
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
}
