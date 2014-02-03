package com.spiddekauga.voider.network;

import java.util.UUID;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;

/**
 * Weapon kryo wrapper for network
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class WeaponDef {
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
}
