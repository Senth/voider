package com.spiddekauga.prototype.network;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;

/**
 * Enemy actor definition
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class OldEnemyDef extends OldActorDef {
	/** Weapon of the enemy, null if it doesn't use one */
	@Tag(19) public OldWeaponDef weapon = null;
	/** Movement type */
	@Tag(20) public String movementType;
	/** Movement speed */
	@Tag(21) public float movementSpeed;

	@Override
	public String toString() {
		return "EnemyDef: {\n\t"
				+ super.toString()
				+ "\n\t" + weapon
				+ "\n\tmovementType: " + movementType
				+ "\n\tmovementSpeed: " + movementSpeed
				+ "\n}";
	}
}
