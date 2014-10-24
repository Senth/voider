package com.spiddekauga.voider.config;


import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import com.spiddekauga.utils.IniClass;
import com.spiddekauga.voider.editor.BulletEditor;
import com.spiddekauga.voider.editor.EnemyEditor;
import com.spiddekauga.voider.editor.ShipEditor;
import com.spiddekauga.voider.game.actors.ActorShapeTypes;
import com.spiddekauga.voider.scene.Scene;

/**
 * Editor settings
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("javadoc")
public class IC_Editor extends IniClass {
	public IC_Actor actor;
	public IC_Bullet bullet;
	public IC_Enemy enemy;
	public IC_Ship ship;

	public class IC_Actor extends IniClass {
		public IC_Visual visual;

		protected float zoomMin;
		protected float zoomMax;

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
		}

		IC_Actor(Ini ini, Section classSection) {
			super(ini, classSection);
		}

		/**
		 * Get the correct visual type for the specified editor
		 * @param editor the editor to get the correct visual type for
		 * @return correct visual type for the editor, if not found it will return the
		 *         default visual config
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

		public float getZoomMin() {
			return zoomMin;
		}

		public float getZoomMax() {
			return zoomMax;
		}
	}

	public class IC_Bullet extends IniClass {
		public IC_Visual visual;

		/**
		 * Overridden visual options
		 */
		public class IC_Visual extends com.spiddekauga.voider.config.IC_Editor.IC_Actor.IC_Visual {
			IC_Visual(Ini ini, Section classSection) {
				actor.super(ini, classSection);
			}
		}


		IC_Bullet(Ini ini, Section classSection) {
			super(ini, classSection);
		}

	}

	public class IC_Enemy extends IniClass {
		public IC_Visual visual;

		/**
		 * Overridden visual options
		 */
		public class IC_Visual extends com.spiddekauga.voider.config.IC_Editor.IC_Actor.IC_Visual {
			IC_Visual(Ini ini, Section classSection) {
				actor.super(ini, classSection);
			}
		}

		IC_Enemy(Ini ini, Section classSection) {
			super(ini, classSection);
		}
	}

	public class IC_Ship extends IniClass {
		public IC_Visual visual;
		public IC_Settings settings;

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

		}

		IC_Ship(Ini ini, Section classSection) {
			super(ini, classSection);
		}

	}

	IC_Editor(Ini ini, Section classSection) {
		super(ini, classSection);
	}
}
