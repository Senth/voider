package com.spiddekauga.voider.network;

import java.util.UUID;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;

/**
 * Level definition for network
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class LevelDef extends Def {
	/** Starting coordinate of the level (right screen edge) */
	@Tag(11) private float startXCoord = 0;
	/** The actual level id, i.e. not this definition's id */
	@Tag(12) private UUID levelId = null;
	/** Campaign id the level belongs to, null if it doesn't belong to any */
	@Tag(13) private UUID campaignId = null;
	/** Base speed of the level, the actual level speed may vary as it can
	 * be changed by triggers */
	@Tag(14) private float speed = -1;
	/** End of the map (right screen edge) */
	@Tag(15) private float mEndXCoord = -1;
	/** Theme of the level */
	@Tag(16) private String mThemeName = null;
}
