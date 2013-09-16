package com.spiddekauga.voider.game;

import java.util.UUID;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.resources.Def;

/**
 * Definition of a saved game. This essentially holds all dependencies
 * and the associated GameSave resource.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class GameSaveDef extends Def {
	/**
	 * Constructs a definition from a GameSave
	 * @param gameSave the game save to create the definition from
	 */
	public GameSaveDef(GameSave gameSave) {
		mGameSaveId = gameSave.getId();

		addDependency(gameSave.getLevel().getDef());
		addDependency(gameSave.getPlayerActor().getDef());
	}

	/**
	 * Default constructor for JSON
	 */
	public GameSaveDef() {
		// Does nothing
	}

	@Override
	public void write(Json json) {
		super.write(json);
		json.writeValue("mGameSaveId", mGameSaveId);
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		super.read(json, jsonData);
		mGameSaveId = json.readValue("mGameSaveId", UUID.class, jsonData);
	}

	/**
	 * @return Id of the game save associated to this definition
	 */
	public UUID getGameSaveId() {
		return mGameSaveId;
	}

	/** Associated GameSave resource */
	@Tag(77) private UUID mGameSaveId = null;
}
