package com.spiddekauga.voider.resources;


/**
 * Tag for a level or campaign
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Tag {
	/**
	 * Sets the tag information
	 * @param tagId id of the tag to get the name from
	 * @param cVotes number of votes of the tag
	 */
	public void setTag(int tagId, int cVotes) {
		/** @todo convert tagId to TagNames */
		mVotes = cVotes;
	}

	/**
	 * @return tag enumeration
	 */
	public TagNames getTagName() {
		return mTagName;
	}

	/**
	 * @return number of votes
	 */
	public int getVoteCount() {
		return mVotes;
	}

	/**
	 * Tag groups
	 */
	public enum TagGroups {
		/** Level difficulty */
		DIFFICULTY,
		/** */
	}

	/**
	 * Tag name enumeration
	 */
	public enum TagNames {

		;

		/**
		 * Initializes the Tag with a name
		 * @param name the name of the tag
		 * @param group the group the tag belongs to
		 */
		TagNames(String name, TagGroups group) {
			mName = name;
		}

		/**
		 * @return name of the tag
		 */
		public String getName() {
			return mName;
		}

		/**
		 * @return group the tag belongs to
		 */
		public TagGroups getGroup() {
			return mGroup;
		}

		/** Name of the tag */
		private String mName;
		/** Tag group */
		private TagGroups mGroup;
	}

	/** Tag name enumeration */
	private TagNames mTagName = null;
	/** Number of votes */
	private int mVotes = 0;
}
