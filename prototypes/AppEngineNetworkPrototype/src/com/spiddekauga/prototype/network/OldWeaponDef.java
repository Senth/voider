package com.spiddekauga.prototype.network;

import java.util.UUID;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;

/**
 * Weapon kryo wrapper for network
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class OldWeaponDef {
	/** Id of the bullet actor */
	@Tag(22) public UUID bulletActorId;
	/** Bullet speed */
	@Tag(23) public float bulletSpeed;
	/** Weapon damage */
	@Tag(24) public float damage;
	/** Minimum cooldown */
	@Tag(25) public float cooldownMin;
	/** Maximum cooldown */
	@Tag(26) public float cooldownMax;

	@Override
	public String toString() {
		return "WeaponDef: {"
				+ "\n\tbulletActorId: " + bulletActorId
				+ "\n\tbulletSpeed: " + bulletSpeed
				+ "\n\tdamage: " + damage
				+ "\n\tcooldownMin: " + cooldownMin
				+ "\n\tcooldownMax: " + cooldownMax
				+ "\n}";
	}
}
