package com.spiddekauga.voider.editor;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.editor.brushes.VectorBrush;
import com.spiddekauga.voider.editor.commands.CResourceCornerRemoveAll;
import com.spiddekauga.voider.editor.tools.AddMoveCornerTool;
import com.spiddekauga.voider.editor.tools.DeleteTool;
import com.spiddekauga.voider.editor.tools.DrawAppendTool;
import com.spiddekauga.voider.editor.tools.DrawEraseTool;
import com.spiddekauga.voider.editor.tools.MoveTool;
import com.spiddekauga.voider.editor.tools.RemoveCornerTool;
import com.spiddekauga.voider.editor.tools.Selection;
import com.spiddekauga.voider.editor.tools.SetCenterTool;
import com.spiddekauga.voider.editor.tools.TouchTool;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.game.actors.ActorShapeTypes;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.InternalNames;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.utils.Pools;

/**
 * 
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class ActorEditor extends Editor implements IActorEditor, IResourceChangeEditor {
	/**
	 * @param gui all UI elements
	 * @param pickRadius picking radius
	 * @param actorType the actor type used in this editor
	 */
	public ActorEditor(Gui gui, float pickRadius, Class<? extends Actor> actorType) {
		super(gui, pickRadius);

		mActorType = actorType;

		mTools[Tools.MOVE.ordinal()] = new MoveTool(mCamera, mWorld, mInvoker, mSelection, this);
		mTools[Tools.DELETE.ordinal()] = new DeleteTool(mCamera, mWorld, mInvoker, mSelection, this);
		mTools[Tools.DRAW_APPEND.ordinal()] = new DrawAppendTool(mCamera, mWorld, mInvoker, mSelection, this, mActorType);
		mTools[Tools.DRAW_ERASE.ordinal()] = new DrawEraseTool(mCamera, mWorld, mInvoker, mSelection, this, mActorType);
		mTools[Tools.ADD_MOVE_CORNER.ordinal()] = new AddMoveCornerTool(mCamera, mWorld, mInvoker, mSelection, this);
		mTools[Tools.REMOVE_CORNER.ordinal()] = new RemoveCornerTool(mCamera, mWorld, mInvoker, mSelection, this);
		mTools[Tools.SET_CENTER.ordinal()] = new SetCenterTool(mCamera, mWorld, mInvoker, mSelection, this, mActorType);
	}

	@Override
	protected void update(float deltaTime) {
		super.update(deltaTime);

		if (mDrawingActor != null && mActorDef.getVisualVars().getShapeType() == ActorShapeTypes.CUSTOM) {
			mDrawingActor.updateEditor();
		}
	}

	@Override
	protected void render() {
		super.render();

		if (mActorDef == null) {
			return;
		}

		if (Config.Graphics.USE_RELEASE_RENDERER && !isSaving() && !isDone()) {
			ShaderProgram defaultShader = ResourceCacheFacade.get(InternalNames.SHADER_DEFAULT);
			if (defaultShader != null) {
				mShapeRenderer.setShader(defaultShader);
			}
			mShapeRenderer.setProjectionMatrix(mCamera.combined);
			mShapeRenderer.push(ShapeType.Filled);
			mShapeRenderer.translate(0, 0, -1);

			// Drawing actor
			if (mDrawingActor != null && mActorDef.getVisualVars().getShapeType() == ActorShapeTypes.CUSTOM) {
				mDrawingActor.render(mShapeRenderer);
				mDrawingActor.renderEditor(mShapeRenderer);
			}

			if (mVectorBrush != null) {
				mVectorBrush.renderEditor(mShapeRenderer);
			}

			mShapeRenderer.translate(0, 0, 1);
			mShapeRenderer.pop();
		}
	}

	@Override
	public void setShapeType(ActorShapeTypes shapeType) {
		if (mActorDef == null) {
			return;
		}

		ActorShapeTypes previousShapeType = mActorDef.getVisualVars().getShapeType();

		mActorDef.getVisualVars().setShapeType(shapeType);


		// Add tool to input multiplexer
		if (shapeType == ActorShapeTypes.CUSTOM && previousShapeType != ActorShapeTypes.CUSTOM) {
			switchTool(Tools.DRAW_APPEND);
			((ActorGui)mGui).resetTools();

			// Add delete tool to input multiplexer
			if (!mInputMultiplexer.getProcessors().contains(mTools[Tools.DELETE.ordinal()], true)) {
				mInputMultiplexer.addProcessor(mTools[Tools.DELETE.ordinal()]);
			}
		}
		// Remove tool from input multiplexer
		else if (shapeType != ActorShapeTypes.CUSTOM && previousShapeType == ActorShapeTypes.CUSTOM) {
			switchTool(Tools.NONE);
			mInputMultiplexer.removeProcessor(mTools[Tools.DELETE.ordinal()]);
		}

		setUnsaved();
	}

	@Override
	public ActorShapeTypes getShapeType() {
		if (mActorDef != null) {
			return mActorDef.getVisualVars().getShapeType();
		} else {
			return ActorShapeTypes.CIRCLE;
		}
	}

	@Override
	public void setShapeRadius(float radius) {
		if (mActorDef == null) {
			return;
		}
		mActorDef.getVisualVars().setShapeRadius(radius);
		setUnsaved();
	}

	@Override
	public float getShapeRadius() {
		if (mActorDef != null) {
			return mActorDef.getVisualVars().getShapeRadius();
		} else {
			return 0;
		}
	}

	@Override
	public void setShapeWidth(float width) {
		if (mActorDef == null) {
			return;
		}
		mActorDef.getVisualVars().setShapeWidth(width);
		setUnsaved();
	}

	@Override
	public float getShapeWidth() {
		if (mActorDef != null) {
			return mActorDef.getVisualVars().getShapeWidth();
		} else {
			return 0;
		}
	}

	@Override
	public void setShapeHeight(float height) {
		if (mActorDef == null) {
			return;
		}
		mActorDef.getVisualVars().setShapeHeight(height);
		setUnsaved();
	}

	@Override
	public float getShapeHeight() {
		if (mActorDef != null) {
			return mActorDef.getVisualVars().getShapeHeight();
		} else {
			return 0;
		}
	}

	@Override
	public void resetCenterOffset() {
		if (mActorDef == null) {
			return;
		}
		// Save diff offset and move the actor in the opposite direction...
		Vector2 diffOffset = null;
		if (mDrawingActor != null) {
			mDrawingActor.destroyBody();

			diffOffset = Pools.vector2.obtain();
			diffOffset.set(mActorDef.getVisualVars().getCenterOffset());
		}

		mActorDef.getVisualVars().resetCenterOffset();

		if (mDrawingActor != null) {
			diffOffset.sub(mActorDef.getVisualVars().getCenterOffset());
			diffOffset.add(mDrawingActor.getPosition());
			mDrawingActor.setPosition(diffOffset);
			mDrawingActor.createBody();
			Pools.vector2.free(diffOffset);
		}

		setUnsaved();
	}

	@Override
	public void setCenterOffset(Vector2 newCenter) {
		if (mActorDef == null) {
			return;
		}
		// Save diff offset and move the actor in the opposite direction...
		Vector2 diffOffset = null;
		if (mDrawingActor != null) {
			mDrawingActor.destroyBody();

			diffOffset = Pools.vector2.obtain();
			diffOffset.set(mActorDef.getVisualVars().getCenterOffset());
		}

		mActorDef.getVisualVars().setCenterOffset(newCenter);

		if (mDrawingActor != null) {
			diffOffset.sub(mActorDef.getVisualVars().getCenterOffset());
			diffOffset.add(mDrawingActor.getPosition());
			mDrawingActor.setPosition(diffOffset);
			mDrawingActor.createBody();
			Pools.vector2.free(diffOffset);
		}

		setUnsaved();
	}

	@Override
	public Vector2 getCenterOffset() {
		if (mActorDef != null) {
			return mActorDef.getVisualVars().getCenterOffset();
		} else {
			return new Vector2();
		}
	}

	@Override
	public void setName(String name) {
		if (mActorDef == null) {
			return;
		}
		mActorDef.setName(name);

		setUnsaved();
	}

	@Override
	public String getName() {
		if (mActorDef != null) {
			return mActorDef.getName();
		} else {
			return "";
		}
	}

	@Override
	public void setDescription(String description) {
		if (mActorDef == null) {
			return;
		}
		mActorDef.setDescription(description);

		setUnsaved();
	}

	@Override
	public String getDescription() {
		if (mActorDef != null) {
			return mActorDef.getDescription();
		} else {
			return "";
		}
	}

	@Override
	public void switchTool(Tools tool) {
		if (mTools[mActiveTool.ordinal()] != null) {
			mTools[mActiveTool.ordinal()].deactivate();

			// Never remove delete tool
			if (mActiveTool != Tools.DELETE) {
				mInputMultiplexer.removeProcessor(mTools[mActiveTool.ordinal()]);
			}
		}

		mActiveTool = tool;

		if (mTools[mActiveTool.ordinal()] != null) {
			mTools[mActiveTool.ordinal()].activate();

			// Never add delete tool
			if (mActiveTool != Tools.DELETE) {
				mInputMultiplexer.addProcessor(mTools[mActiveTool.ordinal()]);
			}
		}
	}

	@Override
	public Tools getActiveTool() {
		return mActiveTool;
	}

	/**
	 * Sets the actor definition
	 * @param actorDef the actor definition
	 */
	protected void setActorDef(ActorDef actorDef) {
		mActorDef = actorDef;

		((DrawAppendTool)mTools[Tools.DRAW_APPEND.ordinal()]).setActorDef(mActorDef);

		if (mActorDef.getVisualVars().getShapeType() == ActorShapeTypes.CUSTOM) {
			if (mDrawingActor == null) {
				mDrawingActor = newActor();
				mDrawingActor.createBody();
				mSelection.selectResource(mDrawingActor);
			}

			mDrawingActor.setDef(mActorDef);
			switchTool(Tools.MOVE);
			((ActorGui)mGui).resetTools();
			// Add delete tool to input multiplexer
			if (!mInputMultiplexer.getProcessors().contains(mTools[Tools.DELETE.ordinal()], true)) {
				mInputMultiplexer.addProcessor(mTools[Tools.DELETE.ordinal()]);
			}
		} else {
			if (mDrawingActor != null) {
				mSelection.deselectResource(mDrawingActor);
				mDrawingActor.dispose();
				mDrawingActor = null;
			}

			switchTool(Tools.NONE);
			mInputMultiplexer.removeProcessor(mTools[Tools.DELETE.ordinal()]);
		}
	}

	@Override
	public boolean isDrawing() {
		if (mTools[mActiveTool.ordinal()] != null) {
			return mTools[mActiveTool.ordinal()].isDrawing();
		} else {
			return false;
		}
	}

	@Override
	public void onResourceAdded(IResource resource) {
		if (resource instanceof Actor) {
			mDrawingActor = (Actor) resource;

			// Set position other than center
			//			Vector2 worldPosition = Pools.vector2.obtain();
			//			screenToWorldCoord(mCamera, Gdx.graphics.getWidth() / 3, Gdx.graphics.getHeight() / 3, worldPosition, false);
			//			mDrawingActor.setPosition(worldPosition);
			//			Pools.vector2.free(worldPosition);

			setUnsaved();
		}
		else if (resource instanceof VectorBrush) {
			mVectorBrush = (VectorBrush) resource;
		}
	}

	@Override
	public void onResourceRemoved(IResource resource) {
		if (resource instanceof Actor) {
			mInvoker.execute(new CResourceCornerRemoveAll(mActorDef.getVisualVars(), this), true);
			mDrawingActor = null;
			setUnsaved();
		}
		else if (resource instanceof VectorBrush) {
			mVectorBrush = null;
		}
	}

	@Override
	public void onResourceChanged(IResource resource) {
		// Does nothing
	}

	@Override
	public void setStartingAngle(float angle) {
		if (mActorDef == null) {
			return;
		}
		mActorDef.setStartAngleDeg(angle);
		setUnsaved();
	}

	@Override
	public float getStartingAngle() {
		if (mActorDef != null) {
			return mActorDef.getStartAngleDeg();
		} else {
			return 0;
		}
	}

	@Override
	public void setRotationSpeed(float rotationSpeed) {
		if (mActorDef == null) {
			return;
		}
		mActorDef.setRotationSpeedDeg(rotationSpeed);
		setUnsaved();
	}

	@Override
	public float getRotationSpeed() {
		if (mActorDef != null) {
			return mActorDef.getRotationSpeedDeg();
		} else {
			return 0;
		}
	}

	/**
	 * Creates a new actor of the current actor type via the default constructor.
	 * If an actor definition has been set, this will also set that definition,
	 * else you need to set this manually if it hasn't been set through the
	 * actor's default constructor.
	 * @return new actor of the current actor type.
	 */
	protected Actor newActor() {
		try {
			Constructor<?> constructor = mActorType.getConstructor();
			Actor actor = (Actor) constructor.newInstance();
			actor.setSkipRotating(true);

			if (mActorDef != null) {
				actor.setDef(mActorDef);
			}

			return actor;

		} catch (Exception e) {
			Gdx.app.error("ActorTool", e.toString());
			e.printStackTrace();
		}

		return null;
	}



	@Override
	public ArrayList<Def> getNonPublishedDependencies() {
		if (mActorDef != null) {
			@SuppressWarnings("unchecked")
			HashSet<UUID> uuidDeps = Pools.hashSet.obtain();
			@SuppressWarnings("unchecked")
			ArrayList<Def> dependencies = Pools.arrayList.obtain();

			getNonPublishedDependencies(mActorDef, uuidDeps, dependencies);

			Pools.hashSet.free(uuidDeps);
			return dependencies;
		}
		return null;
	}

	@Override
	public void publishDef() {
		// TODO Auto-generated method stub

	}


	/** The actor type */
	private Class<? extends Actor> mActorType;
	/** Vector brush to render when drawing custom shapes */
	private VectorBrush mVectorBrush = null;
	/** The actor definition */
	private ActorDef mActorDef = null;
	/** Drawing actor */
	private Actor mDrawingActor = null;
	/** Active tool */
	private TouchTool[] mTools = new TouchTool[Tools.values().length];
	/** Current tool state */
	private Tools mActiveTool = Tools.NONE;
	/** Current selection */
	protected Selection mSelection = new Selection();
}
