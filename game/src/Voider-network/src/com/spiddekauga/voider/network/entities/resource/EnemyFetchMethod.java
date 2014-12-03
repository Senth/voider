package com.spiddekauga.voider.network.entities.resource;

import java.util.ArrayList;
import java.util.List;

import com.spiddekauga.voider.game.actors.AimTypes;
import com.spiddekauga.voider.game.actors.MovementTypes;

/**
 * Fetches information about enemies
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class EnemyFetchMethod extends FetchMethod {
	/** Search by text if not empty */
	public String searchString = "";
	/** Search by movement type if not null */
	public ArrayList<MovementTypes> movementTypes = new ArrayList<>();
	/** Search by a movement speed categories, only available for PATH & AI movement types */
	public ArrayList<EnemySpeedSearchRanges> movementSpeedRanges = new ArrayList<>();
	/** Search by has weapon if not null */
	public Boolean hasWeapon = null;
	/** Search by bullet speed, only available if hasWeapon is set to true */
	public ArrayList<BulletSpeedSearchRanges> bulletSpeedRanges = new ArrayList<>();
	/** Search by bullet damage, only available if hasWeapon is set to true */
	public ArrayList<BulletDamageSearchRanges> bulletDamageRanges = new ArrayList<>();
	/** Search by aim type, only available if hasWeapon is set to true */
	public ArrayList<AimTypes> aimTypes = new ArrayList<>();
	/** Search if enemy is destroyed on collide */
	public Boolean destroyOnCollide = null;
	/** Collision damage categories */
	public ArrayList<CollisionDamageSearchRanges> collisionDamageRanges = new ArrayList<>();


	/**
	 * Creates a copy of this method
	 * @return a copy of this method
	 */
	public EnemyFetchMethod copy() {
		EnemyFetchMethod copy = new EnemyFetchMethod();
		copy.nextCursor = nextCursor;
		copy.searchString = searchString;
		copy.movementTypes.addAll(movementTypes);
		if (canUseMovementSpeed()) {
			copy.movementSpeedRanges.addAll(movementSpeedRanges);
		}
		copy.hasWeapon = hasWeapon;
		if (hasWeapon != null && hasWeapon) {
			copy.bulletSpeedRanges.addAll(bulletSpeedRanges);
			copy.bulletDamageRanges.addAll(bulletDamageRanges);
			copy.aimTypes.addAll(aimTypes);
		}
		copy.destroyOnCollide = destroyOnCollide;
		copy.collisionDamageRanges.addAll(collisionDamageRanges);
		return copy;
	}

	@Override
	public MethodNames getMethodName() {
		return MethodNames.ENEMY_FETCH;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		result = prime * result + getListHashCode(collisionDamageRanges, CollisionDamageSearchRanges.values().length);
		result = prime * result + ((destroyOnCollide == null) ? 0 : destroyOnCollide.hashCode());

		// Only check bullet stuff if weapon has been turned on
		if (hasWeapon != null && hasWeapon) {
			result = prime * result + getListHashCode(aimTypes, AimTypes.values().length);
			result = prime * result + getListHashCode(bulletDamageRanges, BulletDamageSearchRanges.values().length);
			result = prime * result + getListHashCode(bulletSpeedRanges, BulletSpeedSearchRanges.values().length);
		}
		result = prime * result + ((hasWeapon == null) ? 0 : hasWeapon.hashCode());

		// Only check movement speed if AI or PATH is set or is empty
		if (canUseMovementSpeed()) {
			result = prime * result + getListHashCode(movementSpeedRanges, EnemySpeedSearchRanges.values().length);
		}
		result = prime * result + getListHashCode(movementTypes, MovementTypes.values().length);
		result = prime * result + ((searchString == null) ? 0 : searchString.hashCode());
		return result;
	}

	/**
	 * @return true if movement speed can be used
	 */
	public boolean canUseMovementSpeed() {
		if (movementTypes.isEmpty() || movementTypes.size() == MovementTypes.values().length) {
			return true;
		}
		if (movementTypes.contains(MovementTypes.PATH) || movementTypes.contains(MovementTypes.AI)) {
			return true;
		}

		return false;
	}

	/**
	 * Get the hash code for a specific array list. If the list is the maximum size an
	 * empty list hash will instead be used
	 * @param list the list to check
	 * @param maxSize maximum size of the list
	 * @return hash code for the list
	 */
	private static int getListHashCode(List<?> list, int maxSize) {
		if (list != null) {
			if (list.size() != maxSize) {
				return list.hashCode();
			} else {
				return EMPTY_LIST_HASH;
			}
		} else {
			return 0;
		}
	}

	/**
	 * Test equals on a list for a specific array. If any of the lists are of maximum
	 * sizes they will be treated as an empty list
	 * @param thisList
	 * @param otherList
	 * @param maxSize
	 * @return true if the lists are equal
	 */
	private static boolean isListEquals(List<?> thisList, List<?> otherList, int maxSize) {
		if (thisList == null) {
			if (otherList != null) {
				return false;
			}
		} else {
			if (otherList == null) {
				return false;
			} else if (!thisList.equals(otherList)) {
				// Check for max size
				if (!((thisList.isEmpty() || thisList.size() == maxSize) && (otherList.isEmpty() || otherList.size() == maxSize))) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		EnemyFetchMethod other = (EnemyFetchMethod) obj;

		if (!isListEquals(collisionDamageRanges, other.collisionDamageRanges, CollisionDamageSearchRanges.values().length)) {
			return false;
		}
		if (destroyOnCollide == null) {
			if (other.destroyOnCollide != null) {
				return false;
			}
		} else if (!destroyOnCollide.equals(other.destroyOnCollide)) {

		}
		if (hasWeapon != null && hasWeapon) {
			if (!isListEquals(aimTypes, other.aimTypes, AimTypes.values().length)) {
				return false;
			}
			if (!isListEquals(bulletDamageRanges, other.bulletDamageRanges, BulletDamageSearchRanges.values().length)) {
				return false;
			}
			if (!isListEquals(bulletSpeedRanges, other.bulletSpeedRanges, BulletSpeedSearchRanges.values().length)) {
				return false;
			}
		}
		if (hasWeapon == null) {
			if (other.hasWeapon != null) {
				return false;
			}
		} else if (!hasWeapon.equals(other.hasWeapon)) {
			return false;
		}
		if (canUseMovementSpeed()) {
			if (!isListEquals(movementSpeedRanges, other.movementSpeedRanges, EnemySpeedSearchRanges.values().length)) {
				return false;
			}
		}
		if (!isListEquals(movementTypes, other.movementTypes, MovementTypes.values().length)) {
			return false;
		}
		if (searchString == null) {
			if (other.searchString != null) {
				return false;
			}
		} else if (!searchString.equals(other.searchString)) {
			return false;
		}
		return true;
	}

	/** Empty list for testing equal and getting hash */
	private static final int EMPTY_LIST_HASH = new ArrayList<>().hashCode();
}
