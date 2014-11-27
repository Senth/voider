package com.spiddekauga.voider.network.entities.resource;

import java.util.ArrayList;

import com.spiddekauga.voider.game.actors.AimTypes;
import com.spiddekauga.voider.game.actors.MovementTypes;
import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Fetches information about enemies
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class EnemyFetchMethod implements IMethodEntity {
	/** Cursor to continue from */
	public String nextCursor = null;
	/** Search by text if not null */
	public String searchString = null;
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


	@Override
	public MethodNames getMethodName() {
		return MethodNames.ENEMY_FETCH;
	}
}
