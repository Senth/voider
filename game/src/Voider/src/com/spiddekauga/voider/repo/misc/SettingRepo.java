package com.spiddekauga.voider.repo.misc;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.repo.Repo;
import com.spiddekauga.voider.repo.misc.SettingLocalRepo.SettingGeneralLocalRepo;
import com.spiddekauga.voider.repo.misc.SettingLocalRepo.SettingSoundLocalRepo;

/**
 * Setting repository
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class SettingRepo extends Repo {
	/**
	 * Private constructor to enforce singleton pattern
	 */
	private SettingRepo() {
		// Does nothing
	}

	/**
	 * @return instance of this class
	 */
	public static SettingRepo getInstance() {
		if (mInstance == null) {
			mInstance = new SettingRepo();
		}
		return mInstance;
	}


	@Override
	public void handleWebResponse(IMethodEntity method, IEntity response) {
		// TODO Auto-generated method stub

	}

	/**
	 * @return sound settings
	 */
	public SettingSoundRepo sound() {
		return mSound;
	}

	/**
	 * @return date settings
	 */
	public SettingDateRepo date() {
		return mDate;
	}

	/**
	 * Date setting repository. Access through SettingRepo,
	 */
	public class SettingDateRepo {
		/**
		 * Hidden constructor
		 */
		private SettingDateRepo() {
			// Does nothing
		}

		/**
		 * @return time format
		 */
		private String getTimeFormat() {
			// TODO
			return null;
		}

		/**
		 * Set the date format (not time)
		 * @param format
		 */
		public void setDateFormat(String format) {
			// TODO
		}

		/**
		 * @return get date format
		 */
		public String getDateFormat() {
			// TODO
			return null;
		}

		/**
		 * Set if the date should use 24 hours or AM/PM
		 * @param time24h true to use 24hr format, false to use AM/PM
		 */
		public void set24h(boolean time24h) {
			// TODO
		}

		/**
		 * @return true if 24hr are used, false if AM/PM is used
		 */
		public boolean is24h() {
			// TODO
			return true;
		}

		/**
		 * Convert a date to human-readable date and time string using the internal date
		 * time format
		 * @param date the date to format
		 * @return human-readable date time string
		 */
		public String getDateTime(Date date) {
			if (mDateTimeFormatter == null) {
				mDateTimeFormatter = new SimpleDateFormat(mGeneralLocalRepo.getDateTime());
			} else if (!mDateTimeFormatter.toPattern().equals(mGeneralLocalRepo.getDateTime())) {
				mDateTimeFormatter.applyPattern(mGeneralLocalRepo.getDateTime());
			}

			return mDateTimeFormatter.format(date);
		}

		/**
		 * Convert a date to human-readable date string using the internal date format
		 * @param date the date to format
		 * @return human-readable date string
		 */
		public String getDate(Date date) {
			if (mDateFormatter == null) {
				mDateFormatter = new SimpleDateFormat(getDateFormat());
			} else if (!mDateFormatter.toPattern().equals(getDateFormat())) {
				mDateFormatter.applyPattern(getDateFormat());
			}

			return mDateFormatter.format(date);
		}

		/**
		 * Convert a date to human-readable time string using the internal time format
		 * @param date the date to format
		 * @return human-readable time string
		 */
		public String getTime(Date date) {
			if (mTimeFormatter == null) {
				mTimeFormatter = new SimpleDateFormat(getTimeFormat());
			} else if (!mTimeFormatter.toPattern().equals(getTimeFormat())) {
				mTimeFormatter.applyPattern(getTimeFormat());
			}

			return mTimeFormatter.format(date);
		}

		private SimpleDateFormat mDateTimeFormatter = null;
		private SimpleDateFormat mDateFormatter = null;
		private SimpleDateFormat mTimeFormatter = null;
		private SettingGeneralLocalRepo mGeneralLocalRepo = SettingLocalRepo.getInstance().general;
	}

	/**
	 * Sound settings repository. Access through SettingRepo.
	 */
	public class SettingSoundRepo {
		/**
		 * Hidden constructor
		 */
		private SettingSoundRepo() {
			// Does nothing
		}

		/**
		 * Set the master volume
		 * @param volume in range [0,1]
		 */
		public void setMasterVolume(float volume) {
			mSoundLocalRepo.setMasterVolume(volume);
		}

		/**
		 * @return master volume in range [0,1]
		 */
		public float getMasterVolume() {
			return mSoundLocalRepo.getMasterVolume();
		}

		/**
		 * Set the sound effects volume
		 * @param volume in range [0,1]
		 */
		public void setEffectsVolume(float volume) {
			mSoundLocalRepo.setEffectsVolume(volume);
		}

		/**
		 * @return sound effects volume in range [0,1]
		 */
		public float getEffectsVolume() {
			return mSoundLocalRepo.getEffectsVolume();
		}

		/**
		 * Get the real sound effects out volume.
		 * @return sound effects volume multiplied with the master volume
		 */
		public float getEffectsVolumeOut() {
			return mSoundLocalRepo.getEffectsVolumeOut();
		}

		/**
		 * Set the music volume
		 * @param volume in range [0,1]
		 */
		public void setMusicVolume(float volume) {
			mSoundLocalRepo.setMusicVolume(volume);
		}

		/**
		 * @return music volume in range [0,1]
		 */
		public float getMusicVolume() {
			return mSoundLocalRepo.getMusicVolume();
		}

		/**
		 * Get the real music out volume
		 * @return music volume multiplied with the master volume
		 */
		public float getMusicVolumeOut() {
			return mSoundLocalRepo.getMusicVolumeOut();
		}

		/**
		 * Set the UI effects volume
		 * @param volume in range [0,1]
		 */
		public void setUiVolume(float volume) {
			mSoundLocalRepo.setUiVolume(volume);
		}

		/**
		 * @return UI effects volume in range [0,1]
		 */
		public float getUiVolume() {
			return mSoundLocalRepo.getUiVolume();
		}

		/**
		 * Get the real UI out volume
		 * @return UI volume multiplied with the master volume
		 */
		public float getUiVolumeOut() {
			return mSoundLocalRepo.getUiVolumeOut();
		}

		private SettingSoundLocalRepo mSoundLocalRepo = SettingLocalRepo.getInstance().sound;
	}

	private SettingDateRepo mDate = new SettingDateRepo();
	private SettingSoundRepo mSound = new SettingSoundRepo();
	private static SettingRepo mInstance = null;
}
