package com.spiddekauga.voider.resources;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.repo.InternalNames;
import com.spiddekauga.voider.utils.Graphics;
import com.spiddekauga.voider.utils.User;

/**
 * Base class for all "definitions", e.g. ActorDef, WeaponDef. All definitions
 * shall derive from this class.
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class Def extends Resource implements IResourceDependency, IResourceRevision, Disposable, IResourceTexture, IResourcePng {
	/**
	 * Default constructor for the resource.
	 */
	public Def() {
		mUniqueId = UUID.randomUUID();
	}

	@Override
	public void set(Resource resource) {
		super.set(resource);

		Def def = (Def) resource;
		mCopyParentId = def.mCopyParentId;
		mRevisedBy = def.mRevisedBy;
		mDate = def.mDate;
		mDescription = def.mDescription;
		mExternalDependencies = def.mExternalDependencies;
		mInternalDependencies = def.mInternalDependencies;
		mName = def.mName;
		mPngBytes = def.mPngBytes;
		mTextureDrawable = def.mTextureDrawable;
		mOriginalCreator = def.mOriginalCreator;
		mRevision = def.mRevision;
		mRevisedByKey = def.mRevisedByKey;
		mOriginalCreatorKey = def.mOriginalCreatorKey;
	}

	@Override
	public <ResourceType> ResourceType copyNewResource() {
		ResourceType copy = super.copyNewResource();

		Def defCopy = (Def)copy;
		defCopy.mCopyParentId = mUniqueId;
		defCopy.mRevisedBy = mUser.getUsername();
		defCopy.mRevisedByKey = mUser.getServerKey();
		/** @todo create numbering of copy name if already a copy */
		defCopy.mName = defCopy.mName + " (copy)";

		return copy;
	}

	@Override
	public int getExternalDependenciesCount() {
		return mExternalDependencies.size();
	}

	@Override
	public int getInternalDependenciesCount() {
		return mInternalDependencies.size();
	}

	/**
	 * @return the name of the definition
	 */
	public final String getName() {
		return mName;
	}

	/**
	 * Sets the name of the definition
	 * @param name the new name of the definition
	 */
	public void setName(String name) {
		mName = name;
	}

	/**
	 * @return the name of the user that revised this definition
	 */
	public final String getRevisedBy() {
		return mRevisedBy;
	}

	/**
	 * @return the original creator of this definition
	 */
	public final String getOriginalCreator() {
		return mOriginalCreator;
	}

	/**
	 * @return the comment/description of the definition
	 */
	public final String getDescription() {
		return mDescription;
	}

	/**
	 * Sets the comment/description of the definition
	 * @param description the new comment
	 */
	public void setDescription(String description) {
		mDescription = description;
	}

	@Override
	public void addDependency(IResource dependency) {
		AtomicInteger cResources = mExternalDependencies.get(dependency.getId());

		if (cResources != null) {
			cResources.incrementAndGet();
		} else {
			mExternalDependencies.put(dependency.getId(), new AtomicInteger(1));
		}
	}

	@Override
	public void addDependency(InternalNames dependency) {
		mInternalDependencies.add(dependency);
	}

	@Override
	public void removeDependency(UUID dependency) {
		// Decrement the value, remove if only one was left...

		AtomicInteger cResources = mExternalDependencies.get(dependency);
		if (cResources != null) {
			cResources.decrementAndGet();
			if (cResources.get() == 0) {
				mExternalDependencies.remove(dependency);
			}
		} else {
			Gdx.app.error("Def", "No dependency found to remove for the current definition");
		}
	}

	@Override
	public void removeDependency(InternalNames dependency) {
		mInternalDependencies.remove(dependency);
	}

	/**
	 * Update the date to the current date
	 * @see #setDate(Date)
	 */
	public void updateDate() {
		mDate = new Date();
	}

	/**
	 * Sets the date
	 * @param date new date of definition
	 * @see #updateDate()
	 */
	public void setDate(Date date) {
		mDate = date;
	}

	/**
	 * @return date of the definition
	 */
	public Date getDate() {
		return mDate;
	}

	/**
	 * @return date string
	 */
	public String getDateString() {
		return User.getGlobalUser().dateToString(mDate);
	}

	/**
	 * @return the revision of the level
	 */
	public int getRevision() {
		return mRevision;
	}
	/**
	 * Sets the revision of the resource
	 * @param revision the new revision of the resource
	 */
	public void setRevision(int revision) {
		mRevision = revision;
	}

	@Override
	public Map<UUID, AtomicInteger> getExternalDependencies() {
		return mExternalDependencies;
	}

	@Override
	public Set<InternalNames> getInternalDependencies() {
		return mInternalDependencies;
	}

	/**
	 * @return if this object is a copy it will return the parent id which it was
	 * copied from, null this is the original.
	 */
	public UUID getCopyParentId() {
		return mCopyParentId;
	}

	/**
	 * @return creator key
	 */
	public String getRevisedByKey() {
		return mRevisedByKey;
	}

	/**
	 * @return original creator key
	 */
	public String getOriginalCreatorKey() {
		return mOriginalCreatorKey;
	}

	/**
	 * Sets the PNG image for the actor definition. This will also create a
	 * texture for this actor.
	 * @param pngBytes bytes for the png image
	 */
	@Override
	public void setPngImage(byte[] pngBytes) {
		mPngBytes = pngBytes;

		disposeTexture();
		createTexture();
	}

	@Override
	public byte[] getPngImage() {
		return mPngBytes;
	}

	/**
	 * Create the texture from the PNG file
	 */
	private void createTexture() {
		if (mPngBytes != null) {
			mTextureDrawable = Graphics.pngToDrawable(mPngBytes);
		}
	}

	@Override
	public TextureRegionDrawable getTextureRegionDrawable() {
		if (mPngBytes != null && mTextureDrawable == null) {
			createTexture();
		}

		return mTextureDrawable;
	}

	@Override
	public void dispose() {
		disposeTexture();
	}

	/**
	 * Disposes the texture
	 */
	protected void disposeTexture() {
		if (mTextureDrawable != null) {
			mTextureDrawable.getRegion().getTexture().dispose();
			mTextureDrawable = null;
		}
	}

	/** Global user */
	private static final User mUser = User.getGlobalUser();

	/** The possible texture of the image, used if mPngBytes are set.
	 * DON'T SAVE THIS as it is automatically generated when this actor def is loaded */
	private TextureRegionDrawable mTextureDrawable = null;


	/** The PNG bytes for the images, null if not used */
	@Tag(108) private byte[] mPngBytes = null;
	/** Dependencies for the resource */
	@Tag(43) private Map<UUID, AtomicInteger> mExternalDependencies = new HashMap<UUID, AtomicInteger>();
	/** Internal dependencies, such as textures, sound, particle effects */
	@Tag(42) private Set<InternalNames> mInternalDependencies = new HashSet<InternalNames>();
	/** Name of the definition */
	@Tag(36) private String mName = Config.Actor.NAME_DEFAULT;
	/** Original creator key */
	@Tag(112) private String mOriginalCreatorKey = mUser.getServerKey();
	/** Original creator name */
	@Tag(41) private String mOriginalCreator = mUser.getUsername();
	/** Creator key */
	@Tag(111) private String mRevisedByKey = mUser.getServerKey();
	/** Creator name */
	@Tag(39) private String mRevisedBy = mUser.getUsername();
	/** Comment of the definition */
	@Tag(38) private String mDescription = "";
	/** Saved date for the definition */
	@Tag(37) private Date mDate = null;
	/** The revision of the definition, this increases after each save */
	@Tag(40) private int mRevision = 0;
	/** When copied, this is the id of the resource we copied */
	@Tag(87) private UUID mCopyParentId = null;


	// Don't forget to add to DefTest!
}
