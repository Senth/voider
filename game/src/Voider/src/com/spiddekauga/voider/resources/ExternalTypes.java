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

/**
 * All the different external resource types
 */
@SuppressWarnings("javadoc")
public enum ExternalTypes {
	BULLET_DEF(1, BulletActorDef.class),
	ENEMY_DEF(2, EnemyActorDef.class),
	PICKUP_DEF(3, PickupActorDef.class),
	PLAYER_DEF(4, PlayerActorDef.class),
	LEVEL_DEF(5, LevelDef.class),
	LEVEL(6, Level.class),
	GAME_SAVE(7, GameSave.class),
	GAME_SAVE_DEF(8, GameSaveDef.class),
	BUG_REPORT(9, BugReportDef.class),

	// NEXT ID: 10

	;
	/**
	 * Constructor which sets the type
	 * @param id unique id of the enumeration
	 * @param type the class type of resource
	 */
	private ExternalTypes(int id, Class<? extends IResource> type) {
		mId = id;
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
	public static ExternalTypes getEnumFromType(Class<? extends IResource> type) {
		return mClassToEnum.get(type);
	}

	/**
	 * Converts an integer id type to an enumeration
	 * @param id the id type to get an enumeration for
	 * @return enumeration of this id type
	 */
	public static ExternalTypes getEnumFromId(int id) {
		return mIdToEnum.get(id);
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