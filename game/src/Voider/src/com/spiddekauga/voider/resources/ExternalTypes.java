package com.spiddekauga.voider.resources;

import java.util.HashMap;

import com.spiddekauga.voider.game.GameSave;
import com.spiddekauga.voider.game.GameSaveDef;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.PickupActorDef;
import com.spiddekauga.voider.game.actors.PlayerActorDef;
import com.spiddekauga.voider.network.entities.UploadTypes;

/**
 * All the different external resource types
 */
@SuppressWarnings("javadoc")
public enum ExternalTypes {
	BULLET_DEF(UploadTypes.BULLET_DEF, BulletActorDef.class),
	ENEMY_DEF(UploadTypes.ENEMY_DEF, EnemyActorDef.class),
	PICKUP_DEF(UploadTypes.PICKUP_DEF, PickupActorDef.class),
	PLAYER_DEF(UploadTypes.PLAYER_DEF, PlayerActorDef.class),
	LEVEL_DEF(UploadTypes.LEVEL_DEF, LevelDef.class),
	LEVEL(UploadTypes.LEVEL, Level.class),
	GAME_SAVE(UploadTypes.GAME_SAVE, GameSave.class),
	GAME_SAVE_DEF(UploadTypes.GAME_SAVE_DEF, GameSaveDef.class),
	BUG_REPORT(UploadTypes.BUG_REPORT, BugReportDef.class),
	CAMPAIGN_DEF(UploadTypes.CAMPAIGN_DEF, null), // TODO

	;
	/**
	 * Constructor which sets the type
	 * @param uploadType the connected upload type for this external type
	 * @param type the class type of resource
	 */
	private ExternalTypes(UploadTypes uploadType, Class<? extends IResource> type) {
		mId = uploadType.getId();
		mType = type;
	}

	/**
	 * @return type of the resource
	 */
	public Class<? extends IResource> getClassType() {
		return mType;
	}

	/**
	 * @return id of the enumeration
	 */
	public int getId() {
		return mId;
	}

	/**
	 * Converts a class type to an enumeration
	 * @param type the type to get an enumeration for
	 * @return enumeration of this type
	 */
	public static ExternalTypes fromType(Class<? extends IResource> type) {
		return mClassToEnum.get(type);
	}

	/**
	 * Converts an integer id type to an enumeration
	 * @param id the id type to get an enumeration for
	 * @return enumeration of this id type
	 */
	public static ExternalTypes fromId(int id) {
		return mIdToEnum.get(id);
	}

	/**
	 * Converts an upload type enumeration to an external type enumeration
	 * @param uploadType the upload type to get an external type for
	 * @return external type enumeration of uploadType
	 */
	public static ExternalTypes fromUploadType(UploadTypes uploadType) {
		return mIdToEnum.get(uploadType.getId());
	}

	/** Unique integer id of the enum */
	private int mId;
	/** Resource type */
	private Class<? extends IResource> mType;
	/** All enumeration types */
	private static HashMap<Class<? extends IResource>, ExternalTypes> mClassToEnum = new HashMap<>();
	/** From id to type */
	private static HashMap<Integer, ExternalTypes> mIdToEnum = new HashMap<>();

	static {
		for (ExternalTypes externalTypes : ExternalTypes.values()) {
			mIdToEnum.put(externalTypes.mId, externalTypes);
			mClassToEnum.put(externalTypes.mType, externalTypes);
		}
	}
}