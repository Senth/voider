package com.spiddekauga.voider.config;


import com.spiddekauga.utils.IniClass;
import com.spiddekauga.voider.editor.BulletEditor;
import com.spiddekauga.voider.editor.EnemyEditor;
import com.spiddekauga.voider.editor.ShipEditor;
import com.spiddekauga.voider.game.actors.ActorShapeTypes;
import com.spiddekauga.voider.game.actors.ActorTypes;
import com.spiddekauga.utils.scene.ui.Scene;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

/**
 * Editor settings
 */
@SuppressWarnings("javadoc")
public class IC_Editor extends IniClass {
public IC_Actor actor;
public IC_Bullet bullet;
public IC_Enemy enemy;
public IC_Ship ship;
public IC_General general;
public IC_Level level;

IC_Editor(Ini ini, Section classSection) {
	super(ini, classSection);
}


/**
 * Level editor Values
 */
public class IC_Level extends IniClass {
	protected float publishLengthMin;
	protected float speedMin;
	protected float speedMax;
	protected float speedDefault;
	protected float speedStepSize;
	protected float zoomMin;
	protected float zoomMax;
	protected float enemySnapDistance;
	protected float enemySnapDistanceSq;

	private IC_Level(Ini ini, Section classSection) {
		super(ini, classSection);
	}

	public float getPublishLengthMin() {
		return publishLengthMin;
	}

	public float getSpeedMin() {
		return speedMin;
	}

	public float getSpeedMax() {
		return speedMax;
	}

	public float getSpeedDefault() {
		return speedDefault;
	}

	public float getSpeedStepSize() {
		return speedStepSize;
	}

	public float getZoomMin() {
		return zoomMin;
	}

	public float getZoomMax() {
		return zoomMax;
	}

	public float getEnemySnapDistance() {
		return enemySnapDistance;
	}

	public float getEnemySnapDistanceSq() {
		return enemySnapDistanceSq;
	}
}

/**
 * General Editor settings
 */
public class IC_General extends IniClass {
	protected float autoSaveTime;
	protected float autoSaveTimeOnInactivity;
	protected int nameLengthMax;
	protected int descriptionLengthMax;
	protected int storyLengthMax;

	private IC_General(Ini ini, Section classSection) {
		super(ini, classSection);
	}

	public float getAutoSaveTime() {
		return autoSaveTime;
	}

	public float getAutoSaveTimeOnInactivity() {
		return autoSaveTimeOnInactivity;
	}

	public int getNameLengthMax() {
		return nameLengthMax;
	}

	public int getDescriptionLengthMax() {
		return descriptionLengthMax;
	}

	public int getStoryLengthMax() {
		return storyLengthMax;
	}
}

/**
 * Common Actor Editor values
 */
public class IC_Actor extends IniClass {
	public IC_Visual visual;
	public IC_Collision collision;

	protected float zoomMin;
	protected float zoomMax;

	IC_Actor(Ini ini, Section classSection) {
		super(ini, classSection);
	}

	/**
	 * Get the correct visual type for the specified editor
	 * @param editor the editor to get the correct visual type for
	 * @return correct visual type for the editor, if not found it will return the default visual
	 * config
	 */
	public IC_Visual getVisual(Scene editor) {
		if (editor instanceof BulletEditor) {
			return bullet.visual;
		} else if (editor instanceof EnemyEditor) {
			return enemy.visual;
		} else if (editor instanceof ShipEditor) {
			return ship.visual;
		} else {
			return visual;
		}
	}

	/**
	 * Get the correct visual type for the specified actor type
	 * @param actorType the actor type
	 * @return correct visual type for the actor type, if not found it will return the default
	 * visual config
	 */
	public IC_Visual getVisual(ActorTypes actorType) {
		switch (actorType) {
		case BULLET:
			return bullet.visual;

		case ENEMY:
			return enemy.visual;

		case PLAYER:
			return ship.visual;

		case BOSS:
		case INVALID:
		case PICKUP:
		case STATIC_TERRAIN:
		default:
			return actor.visual;
		}
	}

	public float getZoomMin() {
		return zoomMin;
	}

	public float getZoomMax() {
		return zoomMax;
	}

	/**
	 * Visual options
	 */
	public class IC_Visual extends IniClass {
		protected float rotateSpeedMin;
		protected float rotateSpeedMax;
		protected float rotateSpeedDefault;
		protected float rotateSpeedStepSize;
		protected float drawNewCornerDistMinSq;
		protected float drawCornerAngleMin;
		protected float newCornerDistMaxSq;
		protected float radiusMin;
		protected float radiusMax;
		protected float radiusDefault;
		protected float radiusStepSize;
		protected float sizeMin;
		protected float sizeMax;
		protected float sizeDefault;
		protected float sizeStepSize;
		protected ActorShapeTypes shapeDefault;

		// From image
		protected float imageScaleMin;
		protected float imageScaleMax;
		protected float imageScaleStepSize;
		protected float imageScaleDefault;
		protected float imageDistMin;
		protected float imageDistMax;
		protected float imageDistStepSize;
		protected float imageDistDefault;
		protected float imageAngleMin;
		protected float imageAngleMax;
		protected float imageAngleStepSize;
		protected float imageAngleDefault;

		// Fixtures (usually never changed)
		protected float densityMin;
		protected float densityMax;
		protected float densityStepSize;
		protected float densityDefault;
		protected float frictionMin;
		protected float frictionMax;
		protected float frictionStepSize;
		protected float frictionDefault;
		protected float elasticityMin;
		protected float elasticityMax;
		protected float elasticityStepSize;
		protected float elasticityDefault;

		IC_Visual(Ini ini, Section classSection) {
			super(ini, classSection);
		}

		public float getRotateSpeedMin() {
			return rotateSpeedMin;
		}

		public float getRotateSpeedMax() {
			return rotateSpeedMax;
		}

		public float getRotateSpeedDefault() {
			return rotateSpeedDefault;
		}

		public float getRotateSpeedStepSize() {
			return rotateSpeedStepSize;
		}

		public float getDrawNewCornerDistMinSq() {
			return drawNewCornerDistMinSq;
		}

		public float getDrawCornerAngleMin() {
			return drawCornerAngleMin;
		}

		public float getNewCornerDistMaxSq() {
			return newCornerDistMaxSq;
		}

		public float getRadiusMin() {
			return radiusMin;
		}

		public float getRadiusMax() {
			return radiusMax;
		}

		public float getRadiusDefault() {
			return radiusDefault;
		}

		public float getRadiusStepSize() {
			return radiusStepSize;
		}

		public float getSizeMin() {
			return sizeMin;
		}

		public float getSizeMax() {
			return sizeMax;
		}

		public float getSizeDefault() {
			return sizeDefault;
		}

		public float getSizeStepSize() {
			return sizeStepSize;
		}

		public ActorShapeTypes getShapeDefault() {
			return shapeDefault;
		}

		public float getDensityMin() {
			return densityMin;
		}

		public float getDensityMax() {
			return densityMax;
		}

		public float getDensityStepSize() {
			return densityStepSize;
		}

		public float getDensityDefault() {
			return densityDefault;
		}

		public float getFrictionMin() {
			return frictionMin;
		}

		public float getFrictionMax() {
			return frictionMax;
		}

		public float getFrictionStepSize() {
			return frictionStepSize;
		}

		public float getFrictionDefault() {
			return frictionDefault;
		}

		public float getElasticityMin() {
			return elasticityMin;
		}

		public float getElasticityMax() {
			return elasticityMax;
		}

		public float getElasticityStepSize() {
			return elasticityStepSize;
		}

		public float getElasticityDefault() {
			return elasticityDefault;
		}

		public float getImageScaleMin() {
			return imageScaleMin;
		}

		public float getImageScaleMax() {
			return imageScaleMax;
		}

		public float getImageScaleStepSize() {
			return imageScaleStepSize;
		}

		public float getImageScaleDefault() {
			return imageScaleDefault;
		}

		public float getImageDistMin() {
			return imageDistMin;
		}

		public float getImageDistMax() {
			return imageDistMax;
		}

		public float getImageDistStepSize() {
			return imageDistStepSize;
		}

		public float getImageDistDefault() {
			return imageDistDefault;
		}

		public float getImageAngleMin() {
			return imageAngleMin;
		}

		public float getImageAngleMax() {
			return imageAngleMax;
		}

		public float getImageAngleStepSize() {
			return imageAngleStepSize;
		}

		public float getImageAngleDefault() {
			return imageAngleDefault;
		}
	}

	/**
	 * Collision options
	 */
	public class IC_Collision extends IniClass {
		protected float damageMin;
		protected float damageMax;
		protected float damageDefault;
		protected float damageStepSize;
		protected boolean destroyByDefault;

		IC_Collision(Ini ini, Section classSection) {
			super(ini, classSection);
		}

		public float getDamageMin() {
			return damageMin;
		}

		public float getDamageMax() {
			return damageMax;
		}

		public float getDamageDefault() {
			return damageDefault;
		}

		public float getDamageStepSize() {
			return damageStepSize;
		}

		public boolean getDestroyByDefault() {
			return destroyByDefault;
		}
	}
}

/**
 * Bullet Editor values
 */
public class IC_Bullet extends IniClass {
	public IC_Visual visual;

	IC_Bullet(Ini ini, Section classSection) {
		super(ini, classSection);
	}

	/**
	 * Overridden visual options
	 */
	public class IC_Visual extends com.spiddekauga.voider.config.IC_Editor.IC_Actor.IC_Visual {
		IC_Visual(Ini ini, Section classSection) {
			actor.super(ini, classSection);
		}
	}

}


/**
 * Enemy Editor values
 */
public class IC_Enemy extends IniClass {
	public IC_Visual visual;
	public IC_Movement movement;
	public IC_Weapon weapon;

	IC_Enemy(Ini ini, Section classSection) {
		super(ini, classSection);
	}

	/**
	 * Overridden visual options
	 */
	public class IC_Visual extends com.spiddekauga.voider.config.IC_Editor.IC_Actor.IC_Visual {
		IC_Visual(Ini ini, Section classSection) {
			actor.super(ini, classSection);
		}
	}

	public class IC_Movement extends IniClass {
		protected float pathOnceResetTime;
		protected float moveSpeedMin;
		protected float moveSpeedMax;
		protected float moveSpeedDefault;
		protected float moveSpeedStepSize;
		protected boolean turningByDefault;
		protected float turnSpeedMin;
		protected float turnSpeedMax;
		protected float turnSpeedDefault;
		protected float turnSpeedStepSize;
		protected float aiDistanceMin;
		protected float aiDistanceMax;
		protected float aiDistanceMinDefault;
		protected float aiDistanceMaxDefault;
		protected float aiDistanceStepSize;
		protected float randomMovementTimeMin;
		protected float randomMovementTimeMax;
		protected float randomMovementTimeMinDefault;
		protected float randomMovementTimeMaxDefault;
		protected float randomMovementTimeStepSize;
		protected boolean randomMovementByDefault;

		protected IC_Movement(Ini ini, Section classSection) {
			super(ini, classSection);
		}

		public float getPathOnceResetTime() {
			return pathOnceResetTime;
		}

		public float getMoveSpeedMin() {
			return moveSpeedMin;
		}

		public float getMoveSpeedMax() {
			return moveSpeedMax;
		}

		public float getMoveSpeedDefault() {
			return moveSpeedDefault;
		}

		public float getMoveSpeedStepSize() {
			return moveSpeedStepSize;
		}

		public boolean isTurningByDefault() {
			return turningByDefault;
		}

		public float getTurnSpeedMin() {
			return turnSpeedMin;
		}

		public float getTurnSpeedMax() {
			return turnSpeedMax;
		}

		public float getTurnSpeedDefault() {
			return turnSpeedDefault;
		}

		public float getTurnSpeedStepSize() {
			return turnSpeedStepSize;
		}

		public float getAiDistanceMin() {
			return aiDistanceMin;
		}

		public float getAiDistanceMax() {
			return aiDistanceMax;
		}

		public float getAiDistanceMinDefault() {
			return aiDistanceMinDefault;
		}

		public float getAiDistanceMaxDefault() {
			return aiDistanceMaxDefault;
		}

		public float getAiDistanceStepSize() {
			return aiDistanceStepSize;
		}

		public float getRandomMovementTimeMin() {
			return randomMovementTimeMin;
		}

		public float getRandomMovementTimeMax() {
			return randomMovementTimeMax;
		}

		public float getRandomMovementTimeMinDefault() {
			return randomMovementTimeMinDefault;
		}

		public float getRandomMovementTimeMaxDefault() {
			return randomMovementTimeMaxDefault;
		}

		public float getRandomMovementTimeStepSize() {
			return randomMovementTimeStepSize;
		}

		public boolean isRandomMovementOnDefault() {
			return randomMovementByDefault;
		}
	}

	public class IC_Weapon extends IniClass {
		protected float rotateSpeedMin;
		protected float rotateSpeedMax;
		protected float rotateSpeedDefault;
		protected float rotateSpeedStepSize;
		protected float startAngleMin;
		protected float startAngleMax;
		protected float startAngleDefault;
		protected float startAngleStepSize;

		protected IC_Weapon(Ini ini, Section classSection) {
			super(ini, classSection);
		}

		public float getRotateSpeedMin() {
			return rotateSpeedMin;
		}

		public float getRotateSpeedMax() {
			return rotateSpeedMax;
		}

		public float getRotateSpeedDefault() {
			return rotateSpeedDefault;
		}

		public float getRotateSpeedStepSize() {
			return rotateSpeedStepSize;
		}

		public float getStartAngleMin() {
			return startAngleMin;
		}

		public float getStartAngleMax() {
			return startAngleMax;
		}

		public float getStartAngleDefault() {
			return startAngleDefault;
		}

		public float getStartAngleStepSize() {
			return startAngleStepSize;
		}
	}
}


/**
 * Ship Editor values
 */
public class IC_Ship extends IniClass {
	public IC_Visual visual;
	public IC_Settings settings;

	IC_Ship(Ini ini, Section classSection) {
		super(ini, classSection);
	}

	/**
	 * Overridden visual options
	 */
	public class IC_Visual extends com.spiddekauga.voider.config.IC_Editor.IC_Actor.IC_Visual {
		IC_Visual(Ini ini, Section classSection) {
			actor.super(ini, classSection);
		}
	}

	/** Ship settings */
	public class IC_Settings extends IniClass {
		protected float forceMin;
		protected float forceMax;
		protected float forceStepSize;
		protected float forceDefault;
		protected float frequencyMin;
		protected float frequencyMax;
		protected float frequencyStepSize;
		protected float frequencyDefault;
		protected float dampeningMin;
		protected float dampeningMax;
		protected float dampeningStepSize;
		protected float dampeningDefault;


		IC_Settings(Ini ini, Section classSection) {
			super(ini, classSection);
		}

		public float getForceMin() {
			return forceMin;
		}

		public float getForceMax() {
			return forceMax;
		}

		public float getForceStepSize() {
			return forceStepSize;
		}

		public float getForceDefault() {
			return forceDefault;
		}

		public float getFrequencyMin() {
			return frequencyMin;
		}

		public float getFrequencyMax() {
			return frequencyMax;
		}

		public float getFrequencyStepSize() {
			return frequencyStepSize;
		}

		public float getFrequencyDefault() {
			return frequencyDefault;
		}

		public float getDampeningMin() {
			return dampeningMin;
		}

		public float getDampeningMax() {
			return dampeningMax;
		}

		public float getDampeningStepSize() {
			return dampeningStepSize;
		}

		public float getDampeningDefault() {
			return dampeningDefault;
		}
	}

}
}
