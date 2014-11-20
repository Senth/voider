package com.spiddekauga.voider.utils.event;


/**
 * All the different game events
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public enum EventTypes {
	// --- USER ---
	/** User logged in */
	USER_LOGIN,
	/** User gone online */
	USER_CONNECTED,
	/** User gone offline */
	USER_DISCONNECTED,
	/** User logged out */
	USER_LOGOUT,
	/** Failed to login user or connect */
	USER_LOGIN_FAILED,

	// --- CAMERA ---
	/** Camera zoom was changed */
	CAMERA_ZOOM_CHANGE,
	/** Camera was moved */
	CAMERA_MOVED,


	// --- SYNC ---
	/** Successfully downloaded user resources */
	SYNC_USER_RESOURCES_DOWNLOAD_SUCCESS,
	/** Failed to download user resources */
	SYNC_USER_RESOURCES_DOWNLOAD_FAILED,
	/** Successfully uploaded and synced ALL user resources */
	SYNC_USER_RESOURCES_UPLOAD_SUCCESS,
	/** Failed to upload user resources */
	SYNC_USER_RESOURCES_UPLOAD_FAILED,
	/** Conflict when uploading user resources */
	SYNC_USER_RESOURCES_UPLOAD_CONFLICT,
	/** Downloaded new community resources */
	SYNC_COMMUNITY_DOWNLOAD_SUCCESS,
	/** Failed to download community resources */
	SYNC_COMMUNITY_DOWNLOAD_FAILED,


	// --- CLIENT VERSION ---
	/** Update is available, but not necessary */
	UPDATE_AVAILABLE,
	/** Update is required */
	UPDATE_REQUIRED,


	// --- SOUND ---
	/** Master volume was changed */
	SOUND_MASTER_VOLUME_CHANGED,
	/** Music volume was changed */
	SOUND_MUSIC_VOLUME_CHANGED,
	/** Game effects volume was changed */
	SOUND_EFFECTS_VOLUME_CHANGED,
	/** UI volume was changed */
	SOUND_UI_VOLUME_CHANGED,
}
