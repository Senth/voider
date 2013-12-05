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
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.spiddekauga.utils.Command;
import com.spiddekauga.utils.CommandSequence;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.Label.LabelStyle;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.utils.scene.ui.TooltipListener;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.app.MainMenu;
import com.spiddekauga.voider.editor.commands.CEditorLoad;
import com.spiddekauga.voider.editor.commands.CEditorNew;
import com.spiddekauga.voider.editor.commands.CEditorSave;
import com.spiddekauga.voider.editor.commands.CLevelRun;
import com.spiddekauga.voider.editor.commands.CSceneReturn;
import com.spiddekauga.voider.editor.commands.CSceneSwitch;
import com.spiddekauga.voider.game.actors.ActorFilterCategories;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.SkinNames;
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

		mStyles.skin.general = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
		mStyles.skin.editor = ResourceCacheFacade.get(ResourceNames.UI_EDITOR_BUTTONS);
		mStyles.textButton.press = mStyles.skin.general.get(SkinNames.General.TEXT_BUTTON_PRESS.toString(), TextButtonStyle.class);
		mStyles.textButton.toggle = mStyles.skin.general.get(SkinNames.General.TEXT_BUTTON_TOGGLE.toString(), TextButtonStyle.class);
		mStyles.textButton.selected = mStyles.skin.general.get(SkinNames.General.TEXT_BUTTON_SELECTED.toString(), TextButtonStyle.class);
		mStyles.slider.standard = mStyles.skin.general.get(SkinNames.General.SLIDER_DEFAULT.toString(), SliderStyle.class);
		mStyles.textField.standard = mStyles.skin.general.get(SkinNames.General.TEXT_FIELD_DEFAULT.toString(), TextFieldStyle.class);
		mStyles.label.standard = mStyles.skin.general.get(SkinNames.General.LABEL_DEFAULT.toString(), LabelStyle.class);
		mStyles.checkBox.checkBox = mStyles.skin.general.get(SkinNames.General.CHECK_BOX_DEFAULT.toString(), CheckBoxStyle.class);
		mStyles.checkBox.radio = mStyles.skin.general.get(SkinNames.General.CHECK_BOX_RADIO.toString(), CheckBoxStyle.class);
		mStyles.scrollPane.noBackground = mStyles.skin.general.get(SkinNames.General.SCROLL_PANE_DEFAULT.toString(), ScrollPaneStyle.class);
		mStyles.scrollPane.windowBackground = mStyles.skin.general.get(SkinNames.General.SCROLL_PANE_WINDOW_BACKGROUND.toString(), ScrollPaneStyle.class);

		mEditorMenu.setTableAlign(Horizontal.LEFT, Vertical.TOP);
		mFileMenu.setTableAlign(Horizontal.RIGHT, Vertical.TOP);
		mToolMenu.setTableAlign(Horizontal.LEFT, Vertical.TOP);
		mToolMenu.setRowAlign(Horizontal.LEFT, Vertical.TOP);
		mMainTable.setTableAlign(Horizontal.RIGHT, Vertical.TOP);
		mMainTable.setRowAlign(Horizontal.RIGHT, Vertical.TOP);

		initEditorMenu();
		initFileMenu();

		mToolMenu.row().setPadTop(getEditorMenuTopPadding());
		mMainTable.row().setPadTop(getEditorMenuTopPadding());
	}

	/**
	 * Shows the first time menu
	 */
	void showFirstTimeMenu() {
		MsgBoxExecuter msgBox = getFreeMsgBox(false);
		msgBox.button("New", new CEditorNew(mEditor));
		msgBox.buttonRow();
		msgBox.button("Load", new CEditorLoad(mEditor));
		msgBox.buttonRow();
		msgBox.button("Main Menu", new CSceneReturn(MainMenu.class));
		showMsgBox(msgBox);
	}

	/**
	 * Initializes the editor menu
	 */
	private void initEditorMenu() {
		Button button;

		/** @todo add campaign editor button */

		// Level editor
		if (Config.Gui.usesTextButtons()) {
			if (this.getClass() == LevelEditorGui.class) {
				button = new TextButton("Level", mStyles.textButton.selected);
			} else {
				button = new TextButton("Level", mStyles.textButton.press);
			}
		} else {
			if (this.getClass() == LevelEditorGui.class) {
				button = new ImageButton(mStyles.skin.editor, EditorIcons.LEVEL_EDITOR_SELECTED.toString());
			} else {
				button = new ImageButton(mStyles.skin.editor, EditorIcons.LEVEL_EDITOR.toString());
			}
		}
		TooltipListener tooltipListener = new TooltipListener(button, "Level Editor", Messages.replaceName(Messages.Tooltip.Menus.Editor.LEVEL_EDITOR,  "level"));
		mEditorMenu.add(button);
		if (this.getClass() != LevelEditorGui.class) {
			new ButtonListener(button, tooltipListener) {
				@Override
				protected void onPressed() {
					switchReturnTo("Level Editor", new CSceneSwitch(LevelEditor.class), UnsavedActions.LEVEL_EDITOR);
				}
			};
		}

		// Enemy editor
		if (Config.Gui.usesTextButtons()) {
			if (this.getClass() == EnemyEditorGui.class) {
				button = new TextButton("Enemy", mStyles.textButton.selected);
			} else {
				button = new TextButton("Enemy", mStyles.textButton.press);
			}
		} else {
			if (this.getClass() == EnemyEditorGui.class) {
				button = new ImageButton(mStyles.skin.editor, EditorIcons.ENEMY_EDITOR_SELECTED.toString());
			} else {
				button = new ImageButton(mStyles.skin.editor, EditorIcons.ENEMY_EDITOR.toString());
			}
		}
		tooltipListener = new TooltipListener(button, "Enemy Editor", Messages.replaceName(Messages.Tooltip.Menus.Editor.ENEMY_EDITOR,  "enemy"));
		mEditorMenu.add(button);
		if (this.getClass() != EnemyEditorGui.class) {
			new ButtonListener(button, tooltipListener) {
				@Override
				protected void onPressed() {
					switchReturnTo("Enemy Editor", new CSceneSwitch(EnemyEditor.class), UnsavedActions.ENEMY_EDITOR);
				}
			};
		}

		// Bullet editor
		if (Config.Gui.usesTextButtons()) {
			if (this.getClass() == BulletEditorGui.class) {
				button = new TextButton("Bullet", mStyles.textButton.selected);
			} else {
				button = new TextButton("Bullet", mStyles.textButton.press);
			}
		} else {
			if (this.getClass() == BulletEditorGui.class) {
				button = new ImageButton(mStyles.skin.editor, EditorIcons.BULLET_EDITOR_SELECTED.toString());
			} else {
				button = new ImageButton(mStyles.skin.editor, EditorIcons.BULLET_EDITOR.toString());
			}
		}
		tooltipListener = new TooltipListener(button, "Bullet Editor", Messages.replaceName(Messages.Tooltip.Menus.Editor.BULLET_EDITOR,  "bullet"));
		mEditorMenu.add(button).setPadRight(Config.Gui.SEPARATE_PADDING);
		if (this.getClass() != BulletEditorGui.class) {
			new ButtonListener(button, tooltipListener) {
				@Override
				protected void onPressed() {
					switchReturnTo("Bullet Editor", new CSceneSwitch(BulletEditor.class), UnsavedActions.ENEMY_EDITOR);
				}
			};
		}
	}

	/**
	 * Adds a button to the editor menu
	 * @param button the button to add to the editor menu
	 */
	protected void addToEditorMenu(Button button) {
		mEditorMenu.add(button);
	}

	/**
	 * Initializes the file menu
	 */
	protected void initFileMenu() {
		Button button;

		// New
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("New", mStyles.textButton.press);
		} else {
			button = new ImageButton(mStyles.skin.editor, EditorIcons.NEW.toString());
		}
		mFileMenu.add(button);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				executeCommandAndCheckSave(new CEditorNew(mEditor), "New " + getResourceTypeName(), "Save first", "Discard current", Messages.getUnsavedMessage(getResourceTypeName(), UnsavedActions.NEW));
			}
		};

		// Save
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Save", mStyles.textButton.press);
		} else {
			button = new ImageButton(mStyles.skin.editor, EditorIcons.SAVE.toString());
		}
		mFileMenu.add(button);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mEditor.saveDef();
			}
		};

		// Load
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Load", mStyles.textButton.press);
		} else {
			button = new ImageButton(mStyles.skin.editor, EditorIcons.LOAD.toString());
		}
		mFileMenu.add(button);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				executeCommandAndCheckSave(new CEditorLoad(mEditor), "Load another " + getResourceTypeName(), "Save first", "Discard current", Messages.getUnsavedMessage(getResourceTypeName(), UnsavedActions.LOAD));
			}
		};

		// Duplicate
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Duplicate", mStyles.textButton.press);
		} else {
			button = new ImageButton(mStyles.skin.editor, EditorIcons.DUPLICATE.toString());
		}
		mFileMenu.add(button).setPadRight(Config.Gui.SEPARATE_PADDING);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mEditor.saveDef();
				mEditor.duplicateDef();
			}
		};


		// Undo
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Undo", mStyles.textButton.press);
		} else {
			button = new ImageButton(mStyles.skin.editor, EditorIcons.UNDO.toString());
		}
		mFileMenu.add(button);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mEditor.getInvoker().undo();
			}
		};

		// Redo
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Redo", mStyles.textButton.press);
		} else {
			button = new ImageButton(mStyles.skin.editor, EditorIcons.REDO.toString());
		}
		mFileMenu.add(button).setPadRight(Config.Gui.SEPARATE_PADDING);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mEditor.getInvoker().redo();
			}
		};


		// Run (for level editor)
		if (mEditor instanceof LevelEditor) {
			if (Config.Gui.usesTextButtons()) {
				button = new TextButton("Run", mStyles.textButton.press);
			} else {
				button = new ImageButton(mStyles.skin.editor, EditorIcons.RUN.toString());
			}
			mFileMenu.add(button);
			new ButtonListener(button) {
				@Override
				protected void onPressed() {
					MsgBoxExecuter msgBox = getFreeMsgBox(true);

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
		if (Config.Gui.usesTextButtons()) {
			button = new TextButton("Info", mStyles.textButton.press);
		} else {
			button = new ImageButton(mStyles.skin.editor, EditorIcons.INFO.toString());
		}
		mFileMenu.add(button);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				showInfoDialog();
			}
		};
	}

	/**
	 * Shows the information for the resource we're editing
	 */
	protected abstract void showInfoDialog();

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
			Button saveThenExecuteButton = new TextButton(saveButtonText, mStyles.textButton.press);
			Button justExecuteButton = new TextButton(withoutSaveButtonText, mStyles.textButton.press);

			Command save = new CEditorSave(mEditor);
			Command saveAndExecute = new CommandSequence(save, command);

			MsgBoxExecuter msgBox = getFreeMsgBox(true);

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

			ArrayList<Actor> allActors = mMainTable.getActors(true);
			ArrayList<Actor> actors = mEditorMenu.getActors(true);
			allActors.addAll(actors);
			Pools.arrayList.free(actors);
			actors = mFileMenu.getActors(true);
			allActors.addAll(actors);
			Pools.arrayList.free(actors);
			actors = mToolMenu.getActors(true);
			allActors.addAll(actors);
			Pools.arrayList.free(actors);

			for (Actor actor : allActors) {
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
			Pools.arrayList.free(allActors);


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

	/**
	 * Container for all ui styles
	 */
	@SuppressWarnings("javadoc")
	protected static class UiStyles {
		TextButton textButton = new TextButton();
		Slider slider = new Slider();
		TextField textField = new TextField();
		Skins skin = new Skins();
		Label label = new Label();
		CheckBox checkBox = new CheckBox();
		ScrollPane scrollPane = new ScrollPane();

		static class TextButton {
			TextButtonStyle press = null;
			TextButtonStyle toggle = null;
			TextButtonStyle selected = null;
		}

		static class Slider {
			SliderStyle standard = null;
		}

		static class TextField {
			TextFieldStyle standard = null;
		}

		static class Skins {
			Skin general = null;
			Skin editor = null;
		}

		static class Label {
			LabelStyle standard = null;
		}

		static class CheckBox {
			CheckBoxStyle radio = null;
			CheckBoxStyle checkBox = null;
		}

		static class ScrollPane {
			ScrollPaneStyle noBackground;
			ScrollPaneStyle windowBackground;
		}
	}

	/** All skins and styles */
	protected UiStyles mStyles = new UiStyles();

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
	private ArrayList<Body> mBodies = null;
}
