package com.spiddekauga.voider.server.util;


/**
 * Server configuration
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ServerConfig {
	/** Database tables */
	@SuppressWarnings("javadoc")
	public static class DatastoreTables {
		/** Blob information for all blobs */
		public static final String BLOB_INFO = "__BlobInfo__";
		public static final String USERS = "users";
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


		// -- Columns --
		// Users
		public static class CUsers {
			public static final String USERNAME = "username";
			public static final String EMAIL = "email";
			public static final String PASSWORD = "password";
			public static final String CREATED = "created";
			public static final String LOGGED_IN = "logged-in";
			public static final String PRIVATE_KEY = "private_key";
			public static final String DATE_FORMAT = "date_format";
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

		// Actor stat
		public static class CActorStat {

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
		}
	}

	/** Email administrator */
	public static final String EMAIL_ADMIN = "spiddekauga@voider-game.com";

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
	}
}
