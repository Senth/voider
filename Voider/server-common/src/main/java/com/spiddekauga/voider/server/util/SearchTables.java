package com.spiddekauga.voider.server.util;

/**
 * Tables for search database
 */
@SuppressWarnings("javadoc")
public class SearchTables {
public static final String ENEMY = "enemy_def";
public static final String BULLET = "bullet_def";
public static final String LEVEL = "level_def";

// Common for all resources
public static class SDef {
	public static final String CREATOR = "creator";
	public static final String ORIGINAL_CREATOR = "original_creator";
	public static final String NAME = "name";
	public static final String DATE = "date";
}

// Enemy
public static class SEnemy extends SDef {
	public static final String MOVEMENT_TYPE = "movement_type";
	public static final String MOVEMENT_SPEED = "movement_speed";
	public static final String MOVEMENT_SPEED_CAT = "movement_speed_cat";
	public static final String HAS_WEAPON = "has_weapon";
	public static final String BULLET_SPEED = "bullet_speed";
	public static final String BULLET_SPEED_CAT = "bullet_speed_cat";
	public static final String AIM_TYPE = "aim_type";
	public static final String BULLET_DAMAGE = "bullet_damage";
	public static final String BULLET_DAMAGE_CAT = "bullet_damage_cat";
	public static final String DESTROY_ON_COLLIDE = "destroy_on_collide";
	public static final String COLLISION_DAMAGE = "collision_damage";
	public static final String COLLISION_DAMAGE_CAT = "collision_damage_cat";
}

// Bullet
public static class SBullet extends SDef {

}

// Level
public static class SLevel extends SDef {
	public static final String LEVEL_LENGTH = "level_length";
	public static final String LEVEL_LENGTH_CAT = "level_length_cat";
	public static final String LEVEL_SPEED = "level_speed";
	public static final String LEVEL_SPEED_CAT = "level_speed_cat";
	public static final String TAGS = "tags";
}
}
