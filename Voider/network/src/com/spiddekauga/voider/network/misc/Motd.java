package com.spiddekauga.voider.network.misc;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.utils.IIdStore;
import com.spiddekauga.voider.network.entities.IEntity;

import java.util.Date;
import java.util.HashMap;

/**
 * Message of the Day
 */
public class Motd implements IEntity {
/** When the message was created */
@Tag(18)
public Date created;
/** Message title */
@Tag(19)
public String title;
/** Content of the message */
@Tag(20)
public String content;
/** Message type/severity */
@Tag(21)
public MotdTypes type;

/**
 * Various message types. These sorted by severity
 */
public enum MotdTypes implements IIdStore {
	/** Reserved. Not used atm */
	SEVERE(1),
	/** Usually server maintenance */
	WARNING(2),
	/** Highlighted message */
	HIGHLIGHT(3),
	/** Regular, but not really important information */
	INFO(4),
	/**
	 * Reserved. Not used atm. Sponsored/Featured information, not essential to the game
	 */
	FEATURED(5),

	// Next ID: 6
	;

	private static HashMap<Integer, MotdTypes> mIdToEnum = new HashMap<>();

	static {
		for (MotdTypes type : MotdTypes.values()) {
			mIdToEnum.put(type.mId, type);
		}
	}

	/** Id for saving in datastore */
	private int mId;


	/**
	 * Id for saving in datastore
	 * @param id the id saved in datastore
	 */
	private MotdTypes(int id) {
		mId = id;
	}

	/**
	 * Get the enum with the specified id
	 * @param id id of the enum to get
	 * @return enum with the specified id, null if not found
	 */
	public static MotdTypes fromId(int id) {
		return mIdToEnum.get(id);
	}

	@Override
	public int toId() {
		return mId;
	}
}
}
