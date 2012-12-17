package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.spiddekauga.voider.game.ActorDef;
import com.spiddekauga.voider.resources.Textures;

/**
 * Boss actor definition, does nothing more than specify that the actor
 * is a boss.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class BossActorDef extends ActorDef {
	/**
	 * Constructor that sets all variables
	 * @param maxLife maximum life of the actor, also starting amount of life
	 * @param textureTypes all the texture types that are used for the actor
	 * @param name name of the actor
	 * @param fixtureDef physical representation of the object
	 * @TODO just a stub for now
	 */
	public BossActorDef(
			float maxLife,
			Textures.Types[] textureTypes,
			String name,
			FixtureDef fixtureDef
			)
	{
		super(maxLife, textureTypes, name, fixtureDef);
	}

	/**
	 * Empty constructor that does nothing. Used for JSON.
	 */
	@SuppressWarnings("unused")
	private BossActorDef() {
	}
}
