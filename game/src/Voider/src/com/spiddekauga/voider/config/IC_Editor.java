package com.spiddekauga.voider.config;


import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import com.spiddekauga.utils.IniClass;

/**
 * Editor settings
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("javadoc")
public class IC_Editor extends IniClass {
	IC_Actor actor;
	IC_Bullet bullet;
	IC_Enemy enemy;
	IC_Ship ship;

	/**
	 * Editor actor options
	 */
	public static class IC_Actor extends IniClass {
		IC_Visual visual;
		protected float zoomMin;
		protected float zoomMax;

		/**
		 * Visual options
		 */
		public static class IC_Visual extends IniClass {
			protected float rotateSpeedMin;
			protected float rotateSpeedMax;
			protected float rotateSpeedDefault;

			// protected float rotateSpeedStepSize;
			// protected float drawNewCornerMinDistSq;
			// protected float drawCornerAngleMin;
			// protected float newCornerDistMaxSq;
			// protected float radiusMin;
			// protected float radiusMax;
			// protected float radiusDefault;
			// protected float radiusStepSize;
			// protected float sizeMin;
			// protected float sizeMax;
			// protected float sizeDefault;
			// protected float sizeStepSize;
			// protected ActorShapeTypes shapeType;

			IC_Visual(Ini ini, Section classSection) {
				super(ini, classSection);
			}

			/**
			 * @return the rotateSpeedMin
			 */
			public float getRotateSpeedMin() {
				return rotateSpeedMin;
			}

			/**
			 * @return the rotateSpeedMax
			 */
			public float getRotateSpeedMax() {
				return rotateSpeedMax;
			}

			/**
			 * @return the rotateSpeedDefault
			 */
			public float getRotateSpeedDefault() {
				return rotateSpeedDefault;
			}

			// /**
			// * @return the rotateSpeedStepSize
			// */
			// public float getRotateSpeedStepSize() {
			// return rotateSpeedStepSize;
			// }
			//
			// /**
			// * @return the drawNewCornerMinDistSq
			// */
			// public float getDrawNewCornerMinDistSq() {
			// return drawNewCornerMinDistSq;
			// }
			//
			// /**
			// * @return the drawCornerAngleMin
			// */
			// public float getDrawCornerAngleMin() {
			// return drawCornerAngleMin;
			// }
			//
			// /**
			// * @return the newCornerDistMaxSq
			// */
			// public float getNewCornerDistMaxSq() {
			// return newCornerDistMaxSq;
			// }
			//
			// /**
			// * @return the radiusMin
			// */
			// public float getRadiusMin() {
			// return radiusMin;
			// }
			//
			// /**
			// * @return the radiusMax
			// */
			// public float getRadiusMax() {
			// return radiusMax;
			// }
			//
			// /**
			// * @return the radiusDefault
			// */
			// public float getRadiusDefault() {
			// return radiusDefault;
			// }
			//
			// /**
			// * @return the radiusStepSize
			// */
			// public float getRadiusStepSize() {
			// return radiusStepSize;
			// }
			//
			// /**
			// * @return the sizeMin
			// */
			// public float getSizeMin() {
			// return sizeMin;
			// }
			//
			// /**
			// * @return the sizeMax
			// */
			// public float getSizeMax() {
			// return sizeMax;
			// }
			//
			// /**
			// * @return the sizeDefault
			// */
			// public float getSizeDefault() {
			// return sizeDefault;
			// }
			//
			// /**
			// * @return the sizeStepSize
			// */
			// public float getSizeStepSize() {
			// return sizeStepSize;
			// }
			//
			// /**
			// * @return the shapeType
			// */
			// public ActorShapeTypes getShapeType() {
			// return shapeType;
			// }
		}

		IC_Actor(Ini ini, Section classSection) {
			super(ini, classSection);
		}

		/**
		 * @return the zoomMin
		 */
		public float getZoomMin() {
			return zoomMin;
		}

		/**
		 * @return the zoomMax
		 */
		public float getZoomMax() {
			return zoomMax;
		}
	}

	/**
	 * Bullet editor options
	 */
	public static class IC_Bullet extends IniClass {
		IC_Visual visual;

		/**
		 * Overridden visual options
		 */
		public static class IC_Visual extends com.spiddekauga.voider.config.IC_Editor.IC_Actor.IC_Visual {
			IC_Visual(Ini ini, Section classSection) {
				super(ini, classSection);
			}
		}


		IC_Bullet(Ini ini, Section classSection) {
			super(ini, classSection);
		}

	}

	/**
	 * Enemy editor options
	 */
	public static class IC_Enemy extends IniClass {
		IC_Visual visual;

		/**
		 * Overridden visual options
		 */
		public static class IC_Visual extends com.spiddekauga.voider.config.IC_Editor.IC_Actor.IC_Visual {
			IC_Visual(Ini ini, Section classSection) {
				super(ini, classSection);
			}
		}

		IC_Enemy(Ini ini, Section classSection) {
			super(ini, classSection);
		}
	}

	/**
	 * Ship editor options
	 */
	public static class IC_Ship extends IniClass {

		/**
		 * Overridden visual options
		 */
		public static class IC_Visual extends com.spiddekauga.voider.config.IC_Editor.IC_Actor.IC_Visual {
			IC_Visual(Ini ini, Section classSection) {
				super(ini, classSection);
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
