package com.spiddekauga.voider.sound;

import com.badlogic.gdx.audio.Music;

/**
 * Interface for interpolation
 */
interface IInterpolationMethod {
/**
 * Interpolates music piece with another one
 * @param current old playing music piece
 * @param next new playing music piece
 * @param maxVolume maximum volume, used to calculate when done. In the interval [0,1]
 * @return true if the interpolation is done
 */
boolean interpolate(Music current, Music next, float maxVolume);
}
