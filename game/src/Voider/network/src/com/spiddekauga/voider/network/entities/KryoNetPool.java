package com.spiddekauga.voider.network.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer;
import com.spiddekauga.utils.kryo.SerializableTaggedFieldSerializer;
import com.spiddekauga.utils.kryo.UUIDSerializer;
import com.spiddekauga.voider.game.actors.AimTypes;
import com.spiddekauga.voider.game.actors.MovementTypes;
import com.spiddekauga.voider.network.analytics.AnalyticsEventEntity;
import com.spiddekauga.voider.network.analytics.AnalyticsEventTypes;
import com.spiddekauga.voider.network.analytics.AnalyticsMethod;
import com.spiddekauga.voider.network.analytics.AnalyticsResponse;
import com.spiddekauga.voider.network.analytics.AnalyticsSceneEntity;
import com.spiddekauga.voider.network.analytics.AnalyticsSessionEntity;
import com.spiddekauga.voider.network.backup.BackupNewBlobsMethod;
import com.spiddekauga.voider.network.backup.BackupNewBlobsResponse;
import com.spiddekauga.voider.network.backup.DeleteAllBlobsMethod;
import com.spiddekauga.voider.network.backup.DeleteAllBlobsResponse;
import com.spiddekauga.voider.network.backup.RestoreBlobsMethod;
import com.spiddekauga.voider.network.backup.RestoreBlobsResponse;
import com.spiddekauga.voider.network.misc.BlobDownloadMethod;
import com.spiddekauga.voider.network.misc.BugReportEntity;
import com.spiddekauga.voider.network.misc.BugReportMethod;
import com.spiddekauga.voider.network.misc.BugReportResponse;
import com.spiddekauga.voider.network.misc.GetUploadUrlMethod;
import com.spiddekauga.voider.network.misc.GetUploadUrlResponse;
import com.spiddekauga.voider.network.misc.Motd;
import com.spiddekauga.voider.network.misc.Motd.MotdTypes;
import com.spiddekauga.voider.network.misc.ServerMessage;
import com.spiddekauga.voider.network.resource.BulletDamageSearchRanges;
import com.spiddekauga.voider.network.resource.BulletDefEntity;
import com.spiddekauga.voider.network.resource.BulletFetchMethod;
import com.spiddekauga.voider.network.resource.BulletFetchResponse;
import com.spiddekauga.voider.network.resource.BulletSpeedSearchRanges;
import com.spiddekauga.voider.network.resource.CampaignDefEntity;
import com.spiddekauga.voider.network.resource.CollisionDamageSearchRanges;
import com.spiddekauga.voider.network.resource.CommentFetchMethod;
import com.spiddekauga.voider.network.resource.CommentFetchResponse;
import com.spiddekauga.voider.network.resource.DefEntity;
import com.spiddekauga.voider.network.resource.DownloadSyncMethod;
import com.spiddekauga.voider.network.resource.DownloadSyncResponse;
import com.spiddekauga.voider.network.resource.EnemyDefEntity;
import com.spiddekauga.voider.network.resource.EnemyFetchMethod;
import com.spiddekauga.voider.network.resource.EnemyFetchResponse;
import com.spiddekauga.voider.network.resource.EnemySpeedSearchRanges;
import com.spiddekauga.voider.network.resource.FetchMethod;
import com.spiddekauga.voider.network.resource.FetchResponse;
import com.spiddekauga.voider.network.resource.FetchStatuses;
import com.spiddekauga.voider.network.resource.LevelDefEntity;
import com.spiddekauga.voider.network.resource.LevelDifficultySearchRanges;
import com.spiddekauga.voider.network.resource.LevelFetchMethod;
import com.spiddekauga.voider.network.resource.LevelFetchResponse;
import com.spiddekauga.voider.network.resource.LevelLengthSearchRanges;
import com.spiddekauga.voider.network.resource.LevelSpeedSearchRanges;
import com.spiddekauga.voider.network.resource.PublishMethod;
import com.spiddekauga.voider.network.resource.PublishResponse;
import com.spiddekauga.voider.network.resource.ResourceBlobEntity;
import com.spiddekauga.voider.network.resource.ResourceConflictEntity;
import com.spiddekauga.voider.network.resource.ResourceDownloadMethod;
import com.spiddekauga.voider.network.resource.ResourceDownloadResponse;
import com.spiddekauga.voider.network.resource.ResourceRevisionBlobEntity;
import com.spiddekauga.voider.network.resource.ResourceRevisionEntity;
import com.spiddekauga.voider.network.resource.RevisionEntity;
import com.spiddekauga.voider.network.resource.UploadTypes;
import com.spiddekauga.voider.network.resource.UserResourceSyncMethod;
import com.spiddekauga.voider.network.resource.UserResourceSyncResponse;
import com.spiddekauga.voider.network.stat.CommentEntity;
import com.spiddekauga.voider.network.stat.HighscoreEntity;
import com.spiddekauga.voider.network.stat.HighscoreGetMethod;
import com.spiddekauga.voider.network.stat.HighscoreGetResponse;
import com.spiddekauga.voider.network.stat.HighscoreSyncEntity;
import com.spiddekauga.voider.network.stat.HighscoreSyncMethod;
import com.spiddekauga.voider.network.stat.HighscoreSyncResponse;
import com.spiddekauga.voider.network.stat.LevelInfoEntity;
import com.spiddekauga.voider.network.stat.LevelStatsEntity;
import com.spiddekauga.voider.network.stat.StatSyncEntity;
import com.spiddekauga.voider.network.stat.StatSyncMethod;
import com.spiddekauga.voider.network.stat.StatSyncResponse;
import com.spiddekauga.voider.network.stat.Tags;
import com.spiddekauga.voider.network.stat.UserLevelStatsEntity;
import com.spiddekauga.voider.network.user.AccountChangeMethod;
import com.spiddekauga.voider.network.user.AccountChangeResponse;
import com.spiddekauga.voider.network.user.LoginMethod;
import com.spiddekauga.voider.network.user.LoginResponse;
import com.spiddekauga.voider.network.user.LoginResponse.RestoreDate;
import com.spiddekauga.voider.network.user.LoginResponse.VersionInformation;
import com.spiddekauga.voider.network.user.LogoutMethod;
import com.spiddekauga.voider.network.user.LogoutResponse;
import com.spiddekauga.voider.network.user.PasswordResetMethod;
import com.spiddekauga.voider.network.user.PasswordResetResponse;
import com.spiddekauga.voider.network.user.PasswordResetSendTokenMethod;
import com.spiddekauga.voider.network.user.PasswordResetSendTokenResponse;
import com.spiddekauga.voider.network.user.RegisterUserMethod;
import com.spiddekauga.voider.network.user.RegisterUserResponse;
import com.spiddekauga.voider.version.Version;

/**
 * Pool for network Kryo instance. When creating a new instance Kryo registers all
 * necessary classes used in the Network.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class KryoNetPool {


	/**
	 * Obtain a kryo object
	 * @return kryo object
	 */
	public Kryo obtain() {
		Kryo kryo = null;
		if (!mPool.isEmpty()) {
			kryo = mPool.remove();
		} else {
			kryo = new Kryo();
			kryo.setRegistrationRequired(true);
			RegisterClasses.registerAll(kryo);
		}

		return kryo;
	}

	/**
	 * Free a kryo object
	 * @param kryo the kryo object to free
	 */
	public void free(Kryo kryo) {
		mPool.offer(kryo);
	}

	/**
	 * Contains all classes that should be registered. Adding new classes shall only be
	 * done at the end of the enumeration. If a class isn't used any longer, don't remove
	 * it; set it as null instead.
	 */
	private enum RegisterClasses {
		LOGIN_RESPONSE(LoginResponse.class, SerializerType.TAGGED),
		LOGIN_RESPONSE_RESTORE_DATE(RestoreDate.class, SerializerType.TAGGED),
		LOGIN_RESPONSE_VERSION_INFORMATION(VersionInformation.class, SerializerType.TAGGED),
		LOGIN_RESPONSE_VERSION_INFORMATION_STATUSES(VersionInformation.Statuses.class),
		LOGIN_RESPONSE_STATUSES(LoginResponse.Statuses.class),
		UUID(UUID.class, new UUIDSerializer()),
		ARRAY_LIST(ArrayList.class),
		MOTD(Motd.class, SerializerType.TAGGED),
		MOTD_TYPES(MotdTypes.class),
		DATE(Date.class),
		LOGIN_METHOD(LoginMethod.class, SerializerType.TAGGED),
		AIM_TYPES(AimTypes.class),
		MOVEMENT_TYPES(MovementTypes.class),
		ANALYTICS_EVENT_ENTITY(AnalyticsEventEntity.class),
		ANALYTICS_EVENT_TYPES(AnalyticsEventTypes.class),
		ANALYTICS_METHOD(AnalyticsMethod.class),
		ANALYTICS_RESPONSE(AnalyticsResponse.class),
		ANALYTICS_SCENE_ENTITY(AnalyticsSceneEntity.class),
		ANALYTICS_SESSION_ENTITY(AnalyticsSessionEntity.class),
		GENERAL_RESPONSE_STATUSES(GeneralResponseStatuses.class),
		BACKUP_NEW_BLOBS_METHOD(BackupNewBlobsMethod.class),
		BACKUP_NEW_BLOBS_RESPONSE(BackupNewBlobsResponse.class),
		DELETE_ALL_BLOBS_METHOD(DeleteAllBlobsMethod.class),
		DELETE_ALL_BLOBS_RESPONSE(DeleteAllBlobsResponse.class),
		RESOURCE_BLOB_ENTITY(ResourceBlobEntity.class),
		RESOURCE_REVISION_BLOB_ENTITY(ResourceRevisionBlobEntity.class),
		UPLOAD_TYPES(UploadTypes.class),
		BULLET_DAMAGE_SEARCH_RANGES(BulletDamageSearchRanges.class),
		BULLET_DEF_ENTITY(BulletDefEntity.class),
		BULLET_FETCH_METHOD(BulletFetchMethod.class),
		BULLET_FETCH_RESPONSE(BulletFetchResponse.class),
		CAMPAIGN_DEF_ENTITY(CampaignDefEntity.class),
		COLLISION_DAMAGE_SEARCH_RANGES(CollisionDamageSearchRanges.class),
		COMMENT_FETCH_METHOD(CommentFetchMethod.class),
		COMMENT_FETCH_RESPONSE(CommentFetchResponse.class),
		DEF_ENTITY(DefEntity.class),
		DOWNLOAD_SYNC_METHOD(DownloadSyncMethod.class),
		DOWNLOAD_SYNC_RESPONSE(DownloadSyncResponse.class),
		DOWNLOAD_SYNC_RESPONSE_STATUSES(DownloadSyncResponse.Statuses.class),
		ENEMY_DEF_ENTITY(EnemyDefEntity.class),
		ENEMY_FETCH_METHOD(EnemyFetchMethod.class),
		ENEMY_FETCH_RESPONSE(EnemyFetchResponse.class),
		ENEMY_SPEED_SEARCH_RANGES(EnemySpeedSearchRanges.class),
		FETCH_METHOD(FetchMethod.class),
		FETCH_RESPONSE(FetchResponse.class),
		FETCH_STATUSES(FetchStatuses.class),
		LEVEL_DEF_ENTITY(LevelDefEntity.class),
		LEVEL_DIFFICULTY_SEARCH_RANGES(LevelDifficultySearchRanges.class),
		LEVEL_FETCH_METHOD(LevelFetchMethod.class),
		LEVEL_FETCH_RESPONSE(LevelFetchResponse.class),
		LEVEL_SPEED_SEARCH_RANGES(LevelSpeedSearchRanges.class),
		PUBLISH_METHOD(PublishMethod.class),
		PUBLISH_RESPONSE(PublishResponse.class),
		PUBLISH_RESPONSE_STATUSES(PublishResponse.Statuses.class),
		RESOURCE_CONFLICT_ENTITY(ResourceConflictEntity.class),
		RESOURCE_DOWNLOAD_METHOD(ResourceDownloadMethod.class),
		RESOURCE_DOWNLOAD_RESPONSE(ResourceDownloadResponse.class),
		RESOURCE_DOWNLOAD_RESPONSE_STATUSES(ResourceDownloadResponse.Statuses.class),
		RESOURCE_REVISION_ENTITY(ResourceRevisionEntity.class),
		REVISION_ENTITY(RevisionEntity.class),
		USER_RESOURCE_SYNC_METHOD(UserResourceSyncMethod.class),
		USER_RESOURCE_SYNC_RESPONSE(UserResourceSyncResponse.class),
		USER_RESOURCE_SYNC_RESPONSE_UPLOAD_STATUSES(UserResourceSyncResponse.UploadStatuses.class),
		HASH_MAP(HashMap.class),
		HASH_SET(HashSet.class),
		BYTE_ARRAY(byte[].class),
		COMMENT_ENTITY(CommentEntity.class),
		HIGHSCORE_ENTITY(HighscoreEntity.class),
		HIGHSCORE_GET_METHOD(HighscoreGetMethod.class),
		HIGHSCORE_GET_METHOD_FETCH(HighscoreGetMethod.Fetch.class),
		HIGHSCORE_GET_RESPONSE(HighscoreGetResponse.class),
		HIGHSCORE_GET_RESPONSE_STATUSES(HighscoreGetResponse.Statuses.class),
		HIGHSCORE_SYNC_ENTITY(HighscoreSyncEntity.class),
		HIGHSCORE_SYNC_METHOD(HighscoreSyncMethod.class),
		HIGHSCORE_SYNC_RESPONSE(HighscoreSyncResponse.class),
		LEVEL_INFO_ENTITY(LevelInfoEntity.class),
		LEVEL_STATS_ENTITY(LevelStatsEntity.class),
		STAT_SYNC_ENTITY(StatSyncEntity.class),
		STAT_SYNC_ENTITY_LEVEL_STAT(StatSyncEntity.LevelStat.class),
		STAT_SYNC_METHOD(StatSyncMethod.class),
		STAT_SYNC_RESPONSE(StatSyncResponse.class),
		TAGS(Tags.class),
		USER_LEVEL_STATS_ENTITY(UserLevelStatsEntity.class),
		ACCOUNT_CHANGE_METHOD(AccountChangeMethod.class),
		ACCOUNT_CHANGE_RESPONSE(AccountChangeResponse.class),
		ACCOUNT_CHANGE_RESPONSE_STATUSES(AccountChangeResponse.AccountChangeStatuses.class),
		LOGOUT_METHOD(LogoutMethod.class),
		LOGOUT_RESPONSE(LogoutResponse.class),
		PASSWORD_RESET_METHOD(PasswordResetMethod.class),
		PASSWORD_RESET_RESPONSE(PasswordResetResponse.class),
		PASSWORD_RESET_RESPONSE_STATUSES(PasswordResetResponse.Statuses.class),
		PASSWORD_RESET_SEND_TOKEN_METHOD(PasswordResetSendTokenMethod.class),
		PASSWORD_RESET_SEND_TOKEN_RESPONSE(PasswordResetSendTokenResponse.class),
		PASSWORD_RESET_SEND_TOKEN_RESPONSE_STATUSES(PasswordResetSendTokenResponse.Statuses.class),
		REGISTER_USER_METHOD(RegisterUserMethod.class),
		REGISTER_USER_RESPONSE(RegisterUserResponse.class),
		REGISTER_USER_RESPONSE_STATUSES(RegisterUserResponse.Statuses.class),
		BLOB_DOWNLOAD_METHOD(BlobDownloadMethod.class),
		BUG_REPORT_ENTITY(BugReportEntity.class),
		BUG_REPORT_ENTITY_BUG_REPORT_TYPES(BugReportEntity.BugReportTypes.class),
		BUG_REPORT_METHOD(BugReportMethod.class),
		BUG_REPORT_RESPONSE(BugReportResponse.class),
		SERVER_MESSAGE(ServerMessage.class),
		SERVER_MESSAGE_MESSAGE_TYPES(ServerMessage.MessageTypes.class),
		GET_UPLOAD_URL_METHOD(GetUploadUrlMethod.class),
		GET_UPLOAD_URL_RESPONSE(GetUploadUrlResponse.class),
		RESTORE_BLOBS_METHOD(RestoreBlobsMethod.class),
		RESTORE_BLOBS_RESPONSE(RestoreBlobsResponse.class),
		LEVEL_LENGTH_SEARCH_RANGES(LevelLengthSearchRanges.class),
		LEVEL_FETCH_METHOD_SORT_ORDERS(LevelFetchMethod.SortOrders.class),
		VERSION(Version.class),
		INT_ARRAY(int[].class),
		BULLET_SPEED_SEARCH_RANGES(BulletSpeedSearchRanges.class),

		// !!! ALWAYS APPEND, NEVER ADD IN THE MIDDLE !!!

		;
		/**
		 * Serializer types
		 */
		private enum SerializerType {
			/** Creates a TaggedFieldSerializer for the type */
			TAGGED,
			/** Creates a SerializableTaggedFieldSerialize for the type */
			SERIALIZABLE_TAGGED,
		}

		/**
		 * Creates a new type to be registered with Kryo using {@link #registerAll(Kryo)}
		 * @param type the type to register, if null it won't register it. Setting to null
		 *        is useful when the class isn't used anymore (doesn't exist) but we still
		 *        need to keep the register order.
		 */
		private RegisterClasses(Class<?> type) {
			mType = type;
		}

		/**
		 * Creates a new type to be registered with Kryo using {@link #registerAll(Kryo)}
		 * and when {@link #createSerializers(Kryo)} is called will created the specified
		 * serializer type
		 * @param type the type to register, if null it won't register it. Setting to null
		 *        is useful when the class isn't used anymore (doesn't exist) but we still
		 *        need to keep the register order.
		 * @param createSerializerType the type of serializer to create when
		 *        {@link #createSerializers(Kryo)} is called.
		 */
		private RegisterClasses(Class<?> type, SerializerType createSerializerType) {
			mType = type;
			mSerializerType = createSerializerType;
		}

		/**
		 * Creates a new type to be registered with Kryo using {@link #registerAll(Kryo)}
		 * @param type the type to register, if null it won't register it. Setting to null
		 *        is useful when the class isn't used anymore (doesn't exist) but we still
		 *        need to keep the register order.
		 * @param serializer the serializer to use for the specified type, if null the
		 *        default serializer will be used instead.
		 */
		private RegisterClasses(Class<?> type, Serializer<?> serializer) {
			mType = type;
			mSerializer = serializer;
		}

		/**
		 * Some classes needs a serializer that requires Kryo in the constructor. These
		 * serializers are created with this method instead.
		 * @param kryo creates the serializers for this Kryo instance.
		 */
		private static void createSerializers(Kryo kryo) {
			// Create tagged or compatible serializers
			for (RegisterClasses registerClass : RegisterClasses.values()) {
				if (registerClass.mSerializerType != null) {
					switch (registerClass.mSerializerType) {
					case TAGGED:
						registerClass.mSerializer = new TaggedFieldSerializer<Object>(kryo, registerClass.mType);
						break;

					case SERIALIZABLE_TAGGED:
						registerClass.mSerializer = new SerializableTaggedFieldSerializer(kryo, registerClass.mType);
						break;
					}
				}
			}
		}

		/**
		 * Registers all classes with serializers.
		 * @param kryo registers the serializers for this Kryo instance.
		 */
		public static void registerAll(Kryo kryo) {
			createSerializers(kryo);

			for (RegisterClasses registerClass : RegisterClasses.values()) {
				if (registerClass.mType != null) {
					if (registerClass.mSerializer == null) {
						kryo.register(registerClass.mType, registerClass.ordinal() + OFFSET);
					} else {
						kryo.register(registerClass.mType, registerClass.mSerializer, registerClass.ordinal() + OFFSET);
					}
				}
			}
		}

		/** Offset for register id, as there exists some default registered types */
		private static final int OFFSET = 50;
		/** Class type to register, if null it is not registered */
		private Class<?> mType;
		/** Serializer to use, if null it uses the default serializer */
		private Serializer<?> mSerializer = null;
		/**
		 * If a serializer of the specified type should be created for this class. If
		 * null, no serializer will be created for this type.
		 */
		private SerializerType mSerializerType = null;
	}

	private BlockingQueue<Kryo> mPool = new LinkedBlockingQueue<>();
}