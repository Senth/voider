package com.spiddekauga.voider.game;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.resources.Def;

import java.util.UUID;

/**
 * Definition of a saved game. This essentially holds all dependencies and the associated GameSave
 * resource.
 */
public class GameSaveDef extends Def {
/** Associated GameSave resource */
@Tag(77)
private UUID mGameSaveId = null;

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
 * Default constructor for Kryo
 */
public GameSaveDef() {
	// Does nothing
}

/**
 * @return Id of the game save associated to this definition
 */
public UUID getGameSaveId() {
	return mGameSaveId;
}
}
