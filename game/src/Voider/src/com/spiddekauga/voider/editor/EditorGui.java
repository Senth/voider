package com.spiddekauga.voider.editor;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.spiddekauga.utils.Command;
import com.spiddekauga.utils.CommandSequence;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.editor.commands.CEditorLoad;
import com.spiddekauga.voider.editor.commands.CEditorNew;
import com.spiddekauga.voider.editor.commands.CEditorSave;
import com.spiddekauga.voider.editor.commands.CLevelRun;
import com.spiddekauga.voider.editor.commands.CSceneSwitch;
import com.spiddekauga.voider.game.actors.ActorFilterCategories;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.SkinNames.EditorIcons;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.utils.Messages;
import com.spiddekauga.voider.utils.Messages.UnsavedActions;
import com.spiddekauga.voider.utils.Pools;

/**
 * Common methods for all editors
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class EditorGui extends Gui {
	/**
	 * Default constructor
	 */
	public EditorGui() {
		getStage().addActor(mEditorMenu);
		getStage().addActor(mFileMenu);
		getStage().addActor(mToolMenu);
	}


	@Override
	public void dispose() {
		mEditorMenu.dispose();
		mFileMenu.dispose();
		mToolMenu.dispose();

		if (mBodies != null) {
			clearCollisionBoxes();
			Pools.arrayList.free(mBodies);
			mBodies = null;
		}

		super.dispose();
	}

	/**
	 * Sets the editor bound to this GUI
	 * @param editor the editor bound to this GUI
	 */
	public void setEditor(Editor editor) {
		mEditor = editor;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();

		mBodies = Pools.arrayList.obtain();

		mGeneralSkin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
		mEditorSkin = ResourceCacheFacade.get(ResourceNames.UI_EDITOR_BUTTONS);
		mTextButtonStyle = mGeneralSkin.get("default", TextButtonStyle.class);
		mTextToggleStyle = mGeneralSkin.get("toggle", TextButtonStyle.class);

		mEditorMenu.setTableAlign(Horizontal.LEFT, Vertical.TOP);
		mFileMenu.setTableAlign(Horizontal.RIGHT, Vertical.TOP);
		mToolMenu.setTableAlign(Horizontal.LEFT, Vertical.TOP);
		mToolMenu.setRowAlign(Horizontal.LEFT, Vertical.TOP);

		initEditorMenu();
		initFileMenu();

		mToolMenu.row().setPadTop(getEditorMenuTopPadding());
		mMainTable.row().setPadTop(getEditorMenuTopPadding());
	}

	/**
	 * Initializes the editor menu
	 */
	private void initEditorMenu() {
		Button button;

		/** @todo add campaign editor button */

		// Level editor
		/** @todo REMOVE text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Level", mTextButtonStyle);
		} else {
			if (this.getClass() == LevelEditorGui.class) {
				button = new ImageButton(mEditorSkin, EditorIcons.LEVEL_EDITOR_SELECTED.toString());
			} else {
				button = new ImageButton(mEditorSkin, EditorIcons.LEVEL_EDITOR.toString());
			}
		}
		mEditorMenu.add(button);
		if (this.getClass() != LevelEditorGui.class) {
			new ButtonListener(button) {
				@Override
				protected void onPressed() {
					switchReturnTo("Level Editor", new CSceneSwitch(LevelEditor.class), UnsavedActions.LEVEL_EDITOR);
				}
			};
		}

		// Enemy editor
		/** @todo REMOVE text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Enemy", mTextButtonStyle);
		} else {
			if (this.getClass() == EnemyEditorGui.class) {
				button = new ImageButton(mEditorSkin, EditorIcons.ENEMY_EDITOR_SELECTED.toString());
			} else {
				button = new ImageButton(mEditorSkin, EditorIcons.ENEMY_EDITOR.toString());
			}
		}
		mEditorMenu.add(button);
		if (this.getClass() != EnemyEditorGui.class) {
			new ButtonListener(button) {
				@Override
				protected void onPressed() {
					switchReturnTo("Enemy Editor", new CSceneSwitch(EnemyEditor.class), UnsavedActions.ENEMY_EDITOR);
				}
			};
		}

		// Bullet editor
		/** @todo REMOVE text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Bullet", mTextButtonStyle);
		} else {
			if (this.getClass() == BulletEditorGui.class) {
				button = new ImageButton(mEditorSkin, EditorIcons.BULLET_EDITOR_SELECTED.toString());
			} else {
				button = new ImageButton(mEditorSkin, EditorIcons.BULLET_EDITOR.toString());
			}
		}
		mEditorMenu.add(button);
		if (this.getClass() != BulletEditorGui.class) {
			new ButtonListener(button) {
				@Override
				protected void onPressed() {
					switchReturnTo("Bullet Editor", new CSceneSwitch(BulletEditor.class), UnsavedActions.ENEMY_EDITOR);
				}
			};
		}
	}

	/**
	 * Initializes the file menu
	 */
	protected void initFileMenu() {
		Button button;

		// New
		/** @todo REMOVE text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("New", mTextButtonStyle);
		} else {
			button = new ImageButton(mEditorSkin, EditorIcons.NEW.toString());
		}
		mFileMenu.add(button);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				executeCommandAndCheckSave(new CEditorNew(mEditor), "New " + getResourceTypeName(), "Save first", "Discard current", Messages.getUnsavedMessage(getResourceTypeName(), UnsavedActions.NEW));
			}
		};

		// Save
		/** @todo REMOVE text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Save", mTextButtonStyle);
		} else {
			button = new ImageButton(mEditorSkin, EditorIcons.SAVE.toString());
		}
		mFileMenu.add(button);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mEditor.saveDef();
			}
		};

		// Load
		/** @todo REMOVE text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Load", mTextButtonStyle);
		} else {
			button = new ImageButton(mEditorSkin, EditorIcons.LOAD.toString());
		}
		mFileMenu.add(button);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				executeCommandAndCheckSave(new CEditorLoad(mEditor), "Load another " + getResourceTypeName(), "Save first", "Discard current", Messages.getUnsavedMessage(getResourceTypeName(), UnsavedActions.LOAD));
			}
		};

		// Duplicate
		/** @todo REMOVE text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Duplicate", mTextButtonStyle);
		} else {
			button = new ImageButton(mEditorSkin, EditorIcons.DUPLICATE.toString());
		}
		mFileMenu.add(button).setPadRight(Config.Gui.SEPARATE_PADDING);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mEditor.saveDef();
				mEditor.duplicateDef();
			}
		};


		// Run (for level editor)
		if (mEditor instanceof LevelEditor) {
			/** @todo REMOVE text button */
			if (Config.Gui.usesTextButtons()) {
				button = new TextButton("Run", mTextButtonStyle);
			} else {
				button = new ImageButton(mEditorSkin, EditorIcons.RUN.toString());
			}
			mFileMenu.add(button);
			new ButtonListener(button) {
				@Override
				protected void onPressed() {
					MsgBoxExecuter msgBox = getFreeMsgBox();

					msgBox.setTitle(Messages.Level.RUN_INVULNERABLE_TITLE);
					msgBox.content(Messages.Level.RUN_INVULNERABLE_CONTENT);
					msgBox.button("Can die", new CLevelRun(false, (LevelEditor)mEditor));
					msgBox.button("Invulnerable", new CLevelRun(true, (LevelEditor)mEditor));
					msgBox.addCancelButtonAndKeys();
					showMsgBox(msgBox);
				}
			};
		}

		// Info
		/** @todo REMOVE text button */
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Info", mTextButtonStyle);
		} else {
			button = new ImageButton(mEditorSkin, EditorIcons.INFO.toString());
		}
		mFileMenu.add(button);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				/** @todo handle info button press */
			}
		};
	}

	/**
	 * Switches or returns to the specified editor or menu.
	 * @param switchReturnTo what we're switching or returning to.
	 * @param command the command to be executed when switching or returning
	 * @param unsavedAction message to be displayed in the message box
	 */
	private void switchReturnTo(String switchReturnTo, Command command, UnsavedActions unsavedAction) {
		boolean switching = command instanceof CSceneSwitch;

		String msgBoxTitle = switching ? "Switch" : "Return";
		msgBoxTitle += " to " + switchReturnTo;

		String saveMessage = switching ? "Save then switch" : "Save then return";
		String justExecuteMessage = switching ? "Switch anyway" : "Return anymay";

		String content = Messages.getUnsavedMessage(getResourceTypeName(), unsavedAction);

		executeCommandAndCheckSave(command, msgBoxTitle, saveMessage, justExecuteMessage, content);
	}

	/**
	 * Run a command. If the editor isn't saved a message box is displayed that asks the player
	 * to either save or discard the resource before executing the command.
	 * @param command the command to run
	 * @param title message box title
	 * @param saveButtonText text for the save button then execute the command
	 * @param withoutSaveButtonText text for the button when to just execute the command without saving
	 * @param content the message to be displayed in the message box
	 */
	protected void executeCommandAndCheckSave(Command command, String title, String saveButtonText, String withoutSaveButtonText, String content) {
		if (!mEditor.isSaved()) {
			Button saveThenExecuteButton = new TextButton(saveButtonText, mTextButtonStyle);
			Button justExecuteButton = new TextButton(withoutSaveButtonText, mTextButtonStyle);

			Command save = new CEditorSave(mEditor);
			Command saveAndExecute = new CommandSequence(save, command);

			MsgBoxExecuter msgBox = getFreeMsgBox();

			msgBox.clear();
			msgBox.setTitle(title);
			msgBox.content(content);
			msgBox.button(saveThenExecuteButton, saveAndExecute);
			msgBox.button(justExecuteButton, command);
			msgBox.addCancelButtonAndKeys();
			showMsgBox(msgBox);
		} else {
			command.execute();
		}
	}

	/**
	 * Reset and add collision boxes for all UI-elements. Should be called
	 * every frame, will reset only when necessary.
	 */
	void resetCollisionBoxes() {
		if (mEditor == null) {
			return;
		}

		if (mLayoutWasValid) {
			mLayoutWasValid = mMainTable.isLayoutValid();
		}


		// Update collision boxes once main table has a valid layout again
		if (!mLayoutWasValid && mMainTable.isLayoutValid()) {
			mMainTable.invalidateHierarchy();

			clearCollisionBoxes();


			// Create new bodies
			BodyDef bodyDef = new BodyDef();
			bodyDef.type = BodyType.StaticBody;
			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.filter.categoryBits = ActorFilterCategories.SCREEN_BORDER;
			fixtureDef.filter.maskBits = ActorFilterCategories.PLAYER;

			PolygonShape polygonShape = new PolygonShape();
			fixtureDef.shape = polygonShape;

			World world = mEditor.getWorld();
			float scale = mEditor.getCamera().viewportWidth / Gdx.graphics.getWidth() * 0.5f;

			Vector2 screenPos = Pools.vector2.obtain();

			ArrayList<Actor> actors = mMainTable.getActors(true);
			for (Actor actor : actors) {

				// Scale width & height
				float worldWidth = actor.getWidth() * scale;
				float worldHeight = actor.getHeight() * scale;
				polygonShape.setAsBox(worldWidth, worldHeight);

				// Scale position
				screenPos.set(actor.getWidth() * 0.5f, actor.getHeight() * 0.5f);
				actor.localToStageCoordinates(screenPos);
				Scene.screenToWorldCoord(mEditor.getCamera(), screenPos, bodyDef.position, false);
				bodyDef.position.y *= -1;

				// Create body
				Body body = world.createBody(bodyDef);
				body.createFixture(fixtureDef);
				mBodies.add(body);
			}

			polygonShape.dispose();
			Pools.arrayList.free(actors);


			Pools.vector2.freeAll(screenPos);
		}
	}

	/**
	 * Clears the collision boxes
	 */
	void clearCollisionBoxes() {
		for (Body body : mBodies) {
			body.getWorld().destroyBody(body);
		}
		mBodies.clear();
	}

	/**
	 * Calculate top editor menu padding
	 * @return pixel top padding for other menus that start from the top
	 */
	protected float getEditorMenuTopPadding() {
		mEditorMenu.layout();
		return mEditorMenu.getHeight() * Config.Gui.PADDING_FROM_EDITOR_MULTIPLIER;
	}

	/**
	 * Calculate maximum tool menu height. Uses editor menu top padding.
	 * @return maximum tool menu height
	 */
	protected float getMaximumToolMenuHeight() {
		return Gdx.graphics.getHeight() - getEditorMenuTopPadding();
	}

	/**
	 * @return resource type name
	 */
	protected abstract String getResourceTypeName();

	/** General UI skin */
	protected Skin mGeneralSkin = null;
	/** Editor UI skin */
	protected Skin mEditorSkin = null;
	/** Text button style */
	protected TextButtonStyle mTextButtonStyle = null;
	/** Text toggle button style */
	protected TextButtonStyle mTextToggleStyle = null;

	/** Editor scene */
	protected Editor mEditor = null;
	/** Editor menu table */
	private AlignTable mEditorMenu = new AlignTable();
	/** Main menu table */
	private AlignTable mFileMenu = new AlignTable();
	/** Tool table */
	protected AlignTable mToolMenu = new AlignTable();
	/** If the main table has a valid layout, false means the collision boxes
	 * will be updated once the main table has a valid layout again */
	private boolean mLayoutWasValid = false;
	/** All UI-bodies for collision */
	@SuppressWarnings("unchecked")
	private ArrayList<Body> mBodies = null;
}
