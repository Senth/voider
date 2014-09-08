package com.spiddekauga.voider.editor;

import java.util.ArrayList;

import org.apache.commons.lang.WordUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.spiddekauga.utils.commands.CInvokerUndoToDelimiter;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.commands.Invoker;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.Background;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.DisableListener;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.utils.scene.ui.TabWidget;
import com.spiddekauga.utils.scene.ui.TooltipWidget;
import com.spiddekauga.utils.scene.ui.TooltipWidget.ITooltip;
import com.spiddekauga.utils.scene.ui.UiFactory.BarLocations;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.editor.commands.CDefHasValidName;
import com.spiddekauga.voider.editor.commands.CEditorDuplicate;
import com.spiddekauga.voider.editor.commands.CEditorLoad;
import com.spiddekauga.voider.editor.commands.CEditorNew;
import com.spiddekauga.voider.editor.commands.CEditorPublish;
import com.spiddekauga.voider.editor.commands.CEditorSave;
import com.spiddekauga.voider.editor.commands.CEditorUndoJustCreated;
import com.spiddekauga.voider.editor.commands.CLevelRun;
import com.spiddekauga.voider.editor.commands.CSceneReturn;
import com.spiddekauga.voider.editor.commands.CSceneSwitch;
import com.spiddekauga.voider.game.actors.ActorFilterCategories;
import com.spiddekauga.voider.menu.MainMenu;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.IResourceTexture;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.resources.SkinNames.EditorIcons;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.utils.Messages;
import com.spiddekauga.voider.utils.Messages.UnsavedActions;
import com.spiddekauga.voider.utils.Pools;
import com.spiddekauga.voider.utils.User;

/**
 * Common methods for all editors
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class EditorGui extends Gui {
	/**
	 * Default constructor
	 */
	public EditorGui() {
		// Does nothing
	}


	@Override
	public void dispose() {
		mEditorMenu.dispose();
		mFileMenu.dispose();
		mToolMenu.dispose();
		mEditMenu.dispose();
		mNameTable.dispose();

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
	public void setEditor(IEditor editor) {
		mEditor = editor;
		mInvoker = mEditor.getInvoker();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();

		if (mEditorMenu.getStage() == null) {
			addActor(mEditorMenu);
			addActor(mEditMenu);
			addActor(mFileMenu);
			addActor(mToolMenu);

			mEditorMenu.setName("EditorMenu");
			mEditMenu.setName("EditMenu");
			mFileMenu.setName("FileMenu");
			mToolMenu.setName("ToolMenu");

			initTooltipBar();
		}

		mBodies = Pools.arrayList.obtain();

		mEditorMenu.setAlignTable(Horizontal.LEFT, Vertical.TOP);
		mEditMenu.setAlignTable(Horizontal.CENTER, Vertical.TOP);
		mFileMenu.setAlignTable(Horizontal.RIGHT, Vertical.TOP);
		mToolMenu.setAlign(Horizontal.LEFT, Vertical.TOP);
		mMainTable.setAlignTable(Horizontal.RIGHT, Vertical.TOP);
		mMainTable.setAlignRow(Horizontal.RIGHT, Vertical.MIDDLE);
		mNameTable.setAlignTable(Horizontal.LEFT, Vertical.TOP);
		mNameTable.setAlignRow(Horizontal.LEFT, Vertical.MIDDLE);

		mEditorMenu.setMargin(mUiFactory.getStyles().vars.paddingOuter);
		mEditMenu.setMargin(mUiFactory.getStyles().vars.paddingOuter);
		mFileMenu.setMargin(mUiFactory.getStyles().vars.paddingOuter);

		initEditorMenu();
		initEditMenu();
		initFileMenu();
		initNameTable();

		float marginOuter = mUiFactory.getStyles().vars.paddingOuter;
		float marginTop = mUiFactory.getStyles().vars.barUpperLowerHeight * 2 + marginOuter;
		float marginBottom = getTopBottomPadding();
		mToolMenu.setMargin(marginTop, marginOuter, marginBottom, marginOuter);

		initTopBottomBar();
	}

	/**
	 * An optional settings menu for the editor. E.g. to switch between visuals and weapon
	 * settings in enemy editor.
	 */
	protected void initSettingsMenu() {
		mSettingTabs
				.setMargin(getTopBottomPadding(), mUiFactory.getStyles().vars.paddingOuter, getTopBottomPadding(),
						mUiFactory.getStyles().vars.paddingOuter).setAlign(Horizontal.RIGHT, Vertical.TOP).setTabAlign(Horizontal.RIGHT)
				.setFillHeight(true).setBackground(new Background(mUiFactory.getStyles().color.widgetBackground))
				.setPaddingContent(mUiFactory.getStyles().vars.paddingInner)
				.setContentWidth((Float) SkinNames.getResource(SkinNames.GeneralVars.RIGHT_PANEL_WIDTH));

		getStage().addActor(mSettingTabs);
	}

	/**
	 * Initializes the top bar and bottom bar
	 */
	private void initTopBottomBar() {
		mUiFactory.addBar(BarLocations.TOP_BOTTOM, getStage());
	}

	/**
	 * Initializes the tooltip bottom bar
	 */
	private void initTooltipBar() {
		mTooltip = mUiFactory.createTooltipWidget();
		addActor(mTooltip);
	}

	/**
	 * Initializes the name table
	 */
	private void initNameTable() {
		float margin = mUiFactory.getStyles().vars.paddingOuter;
		mNameTable.setMargin(getTopBottomPadding(), margin, margin, margin);
		getStage().addActor(mNameTable);
		mNameTable.setKeepHeight(true);
		mNameTable.setHeight(mUiFactory.getStyles().vars.barUpperLowerHeight - margin * 2);

		mNameLabel = new Label("", (LabelStyle) SkinNames.getResource(SkinNames.General.LABEL_EDITOR_NAME));
		mNameTable.row().setFillHeight(true);
		mNameTable.add(mNameLabel).setFillHeight(true);
	}

	/**
	 * Reset name
	 */
	void resetName() {
		if (mNameLabel != null) {
			mNameLabel.setText(mEditor.getName());
		}
	}

	@Override
	public void resetValues() {
		super.resetValues();

		if (mGridRender != null && mGridRenderAbove != null) {
			mGridRender.setChecked(mEditor.isGridOn());
			mGridRenderAbove.setChecked(mEditor.isGridRenderAboveResources());
		}

		if (mEnemyHighlight != null) {
			if (mEditor instanceof LevelEditor) {
				mEnemyHighlight.setChecked(((LevelEditor) mEditor).isEnemyHighlightOn());
			}
		}

		// Name
		resetName();

		// Disabled / Enable actors if published / unpublished
		boolean published = mEditor.isPublished();
		Touchable touchable = published ? Touchable.disabled : Touchable.enabled;
		for (Actor actor : mDisabledWhenPublished) {
			// Button
			if (actor instanceof Button) {
				((Button) actor).setDisabled(published);
			}

			// Text field
			else if (actor instanceof TextField) {
				if (published) {
					((TextField) actor).setDisabled(true);
					actor.setName(Config.Gui.TEXT_FIELD_DISABLED_NAME);
				} else {
					if (actor.isVisible()) {
						((TextField) actor).setDisabled(false);
					}

					actor.setName(null);
				}
			}

			// Other UI types that can't be disabled, make them untouchable
			else {
				actor.setTouchable(touchable);
			}
		}
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

		// Campaign editor
		if (this.getClass() == CampaignEditorGui.class) {
			button = mUiFactory.addImageButton(EditorIcons.CAMPAIGN_EDITOR_SELECTED, mEditorMenu, null, null);
		} else {
			button = mUiFactory.addImageButton(EditorIcons.CAMPAIGN_EDITOR, mEditorMenu, null, null);
		}
		if (this.getClass() != CampaignEditorGui.class) {
			new ButtonListener(button) {
				@Override
				protected void onPressed() {
					switchReturnTo("Campaign Editor", new CSceneSwitch(CampaignEditor.class), UnsavedActions.CAMPAIGN_EDITOR);
				}
			};
		}
		mTooltip.add(button, Messages.EditorTooltips.EDITOR_CAMPAIGN);


		// Level editor
		if (this.getClass() == LevelEditorGui.class) {
			button = mUiFactory.addImageButton(EditorIcons.LEVEL_EDITOR_SELECTED, mEditorMenu, null, null);
		} else {
			button = mUiFactory.addImageButton(EditorIcons.LEVEL_EDITOR, mEditorMenu, null, null);
		}
		if (this.getClass() != LevelEditorGui.class) {
			new ButtonListener(button) {
				@Override
				protected void onPressed() {
					switchReturnTo("Level Editor", new CSceneSwitch(LevelEditor.class), UnsavedActions.LEVEL_EDITOR);
				}
			};
		}
		mTooltip.add(button, Messages.EditorTooltips.EDITOR_LEVEL);


		// Enemy editor
		if (this.getClass() == EnemyEditorGui.class) {
			button = mUiFactory.addImageButton(EditorIcons.ENEMY_EDITOR_SELECTED, mEditorMenu, null, null);
		} else {
			button = mUiFactory.addImageButton(EditorIcons.ENEMY_EDITOR, mEditorMenu, null, null);
		}
		if (this.getClass() != EnemyEditorGui.class) {
			new ButtonListener(button) {
				@Override
				protected void onPressed() {
					switchReturnTo("Enemy Editor", new CSceneSwitch(EnemyEditor.class), UnsavedActions.ENEMY_EDITOR);
				}
			};
		}
		mTooltip.add(button, Messages.EditorTooltips.EDITOR_ENEMY);


		// Bullet editor
		if (this.getClass() == BulletEditorGui.class) {
			button = mUiFactory.addImageButton(EditorIcons.BULLET_EDITOR_SELECTED, mEditorMenu, null, null);
		} else {
			button = mUiFactory.addImageButton(EditorIcons.BULLET_EDITOR, mEditorMenu, null, null);
		}
		if (this.getClass() != BulletEditorGui.class) {
			new ButtonListener(button) {
				@Override
				protected void onPressed() {
					switchReturnTo("Bullet Editor", new CSceneSwitch(BulletEditor.class), UnsavedActions.BULLET_EDITOR);
				}
			};
		}
		mTooltip.add(button, Messages.EditorTooltips.EDITOR_BULLET);
	}

	/**
	 * Initializes the edit menu
	 */
	private void initEditMenu() {
		Button button;

		// Undo
		button = mUiFactory.addImageButton(EditorIcons.UNDO, mEditMenu, null, mDisabledWhenPublished);
		mTooltip.add(button, Messages.EditorTooltips.ACTION_UNDO);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mEditor.getInvoker().undo();
			}
		};

		// Redo
		button = mUiFactory.addImageButton(EditorIcons.REDO, mEditMenu, null, mDisabledWhenPublished);
		mTooltip.add(button, Messages.EditorTooltips.ACTION_REDO);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mEditor.getInvoker().redo();
			}
		};

		// Grid stuff
		if (getClass() != CampaignEditorGui.class) {
			// Grid
			button = mUiFactory.addImageButton(EditorIcons.GRID, mEditMenu, null, null);
			mTooltip.add(button, Messages.EditorTooltips.ACTION_GRID_TOGGLE);
			mGridRender = button;
			DisableListener disableListener = new DisableListener(button);
			new ButtonListener(button) {
				@Override
				protected void onChecked(boolean checked) {
					mEditor.setGrid(checked);
				}
			};

			// Grid above
			button = mUiFactory.addImageButton(EditorIcons.GRID_ABOVE, mEditMenu, null, null);
			mTooltip.add(button, Messages.EditorTooltips.ACTION_GRID_ABOVE);
			mGridRenderAbove = button;
			disableListener.addToggleActor(button);
			new ButtonListener(button) {
				@Override
				protected void onChecked(boolean checked) {
					mEditor.setGridRenderAboveResources(checked);
				}
			};
		}

		// Run & Enemy highlight (for level editor)
		if (mEditor instanceof LevelEditor) {
			// Highlight enemy if it will spawn when test running the level from
			// the current position
			button = mUiFactory.addImageButton(EditorIcons.ENEMY_SPAWN_HIGHLIGHT, mEditMenu, null, null);
			mTooltip.add(button, Messages.EditorTooltips.ACTION_ENEMY_SPAWN);
			mEnemyHighlight = button;
			new ButtonListener(button) {
				@Override
				protected void onChecked(boolean checked) {
					((LevelEditor) mEditor).setEnemyHighlight(checked);
				}
			};

			// Run
			button = mUiFactory.addImageButton(EditorIcons.RUN, mEditMenu, null, null);
			mTooltip.add(button, Messages.EditorTooltips.ACTION_PLAY);
			new ButtonListener(button) {
				@Override
				protected void onPressed() {
					MsgBoxExecuter msgBox = getFreeMsgBox(true);

					msgBox.setTitle(Messages.Level.RUN_INVULNERABLE_TITLE);
					msgBox.content(Messages.Level.RUN_INVULNERABLE_CONTENT);
					msgBox.button("Can die", new CLevelRun(false, (LevelEditor) mEditor));
					msgBox.button("Invulnerable", new CLevelRun(true, (LevelEditor) mEditor));
					msgBox.addCancelButtonAndKeys();
					showMsgBox(msgBox);
				}
			};
		}


	}

	/**
	 * @return get file new tooltip
	 */
	abstract ITooltip getFileNewTooltip();

	/**
	 * @return get file duplicate tooltip
	 */
	abstract ITooltip getFileDuplicateTooltip();

	/**
	 * @return get file publish tooltip
	 */
	abstract ITooltip getFilePublishTooltip();

	/**
	 * @return get file info tooltip
	 */
	abstract ITooltip getFileInfoTooltip();

	/**
	 * Initializes the file menu
	 */
	private void initFileMenu() {
		Button button;

		// New
		button = mUiFactory.addImageButton(EditorIcons.NEW, mFileMenu, null, null);
		ITooltip tooltip = getFileNewTooltip();
		if (tooltip != null) {
			mTooltip.add(button, tooltip);
		}
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				executeCommandAndCheckSave(new CEditorNew(mEditor), "New " + getResourceTypeName(), "Save first", "Discard current",
						Messages.getUnsavedMessage(getResourceTypeName(), UnsavedActions.NEW));
			}
		};

		// Duplicate
		button = mUiFactory.addImageButton(EditorIcons.DUPLICATE, mFileMenu, null, null);
		tooltip = getFileDuplicateTooltip();
		if (tooltip != null) {
			mTooltip.add(button, tooltip);
		}
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				MsgBoxExecuter msgBox = getFreeMsgBox(true);

				msgBox.setTitle("Duplicate");
				msgBox.content(Messages.replaceName(Messages.Editor.DUPLICATE_BOX, getResourceTypeName()));
				msgBox.button("Yes", new CEditorDuplicate(mEditor));
				msgBox.addCancelButtonAndKeys("No");
				showMsgBox(msgBox);
			}
		};

		// Save
		button = mUiFactory.addImageButton(EditorIcons.SAVE, mFileMenu, null, mDisabledWhenPublished);
		mTooltip.add(button, Messages.EditorTooltips.FILE_SAVE);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mEditor.saveDef();
			}
		};

		// Load
		button = mUiFactory.addImageButton(EditorIcons.LOAD, mFileMenu, null, null);
		mTooltip.add(button, Messages.EditorTooltips.FILE_OPEN);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				executeCommandAndCheckSave(new CEditorLoad(mEditor), "Load another " + getResourceTypeName(), "Save first", "Discard current",
						Messages.getUnsavedMessage(getResourceTypeName(), UnsavedActions.LOAD));
			}
		};

		// Publish
		button = mUiFactory.addImageButton(EditorIcons.PUBLISH, mFileMenu, null, mDisabledWhenPublished);
		tooltip = getFilePublishTooltip();
		if (tooltip != null) {
			mTooltip.add(button, tooltip);
		}
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				boolean showPublish = true;

				// For level, make sure level has screen shot before publishing
				if (mEditor instanceof LevelEditor) {
					if (!((LevelEditor) mEditor).hasScreenshot()) {
						showPublish = false;

						MsgBoxExecuter msgBox = getFreeMsgBox(true);
						msgBox.setTitle("No screenshot taken");
						String text = "Please take a screenshot of this level before publishing it. "
								+ "You can do this by test running the level and click on the camera " + "icon in the top bar.";
						Label label = new Label(text, mUiFactory.getStyles().label.standard);
						msgBox.content(label);
						msgBox.addCancelButtonAndKeys("OK");
						label.setWrap(true);
						label.setWidth(Gdx.graphics.getWidth() * 0.5f);
						showMsgBox(msgBox);
					}
				}

				// Check if online
				if (!User.getGlobalUser().isOnline()) {
					showPublish = false;

					MsgBoxExecuter msgBox = getFreeMsgBox(true);
					msgBox.setTitle("Offline");
					String text = "You need to go online to publish the level. Currently this is "
							+ "only possible by either logging out and logging in or restarting " + "the game.";
					Label label = new Label(text, mUiFactory.getStyles().label.standard);
					msgBox.content(label);
					msgBox.addCancelButtonAndKeys("OK");
					label.setWrap(true);
					label.setWidth(Gdx.graphics.getWidth() * 0.5f);
					showMsgBox(msgBox);
				}

				if (showPublish) {
					showPublishDialog();
				}
			}
		};

		// Info
		button = mUiFactory.addImageButton(EditorIcons.INFO, mFileMenu, null, null);
		tooltip = getFileInfoTooltip();
		if (tooltip != null) {
			mTooltip.add(button, tooltip);
		}
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				showInfoDialog();
			}
		};
	}

	/**
	 * Shows the publish message box
	 */
	private void showPublishDialog() {
		MsgBoxExecuter msgBox = getFreeMsgBox(true);
		msgBox.setTitle("Publish");

		AlignTable content = new AlignTable();
		Label label = new Label("", mUiFactory.getStyles().label.highlight);

		float width = Gdx.graphics.getWidth() * 0.5f;

		label.setWrap(true);
		label.setAlignment(Align.center);
		label.setText("You are about to publish this " + getResourceTypeName() + " online. "
				+ "This is irreversible! In addition all dependencies below will be publish.\n\n"
				+ "Once published these cannot be changed or removed; they can however be "
				+ "duplicated to allow you to continue on a next version.");
		content.add(label).setWidth(width);

		// Add external dependencies
		content.row();
		AlignTable depTable = new AlignTable();
		depTable.setAlign(Horizontal.LEFT, Vertical.TOP);
		ArrayList<Def> dependencies = mEditor.getNonPublishedDependencies();

		for (Def dependency : dependencies) {
			depTable.row();

			// Add image
			if (dependency instanceof IResourceTexture) {
				Image image = new Image(((IResourceTexture) dependency).getTextureRegionDrawable());
				depTable.add(image).setSize(50, 50).setPadRight(mUiFactory.getStyles().vars.paddingInner);
			} else {
				depTable.add().setPadRight(50 + mUiFactory.getStyles().vars.paddingInner);
			}

			// Add name
			label = new Label(dependency.getName(), mUiFactory.getStyles().label.standard);
			depTable.add(label);
		}

		Pools.arrayList.free(dependencies);
		ScrollPane scrollPane = new ScrollPane(depTable, (ScrollPaneStyle) SkinNames.getResource(SkinNames.General.SCROLL_PANE_WINDOW_BACKGROUND));
		scrollPane.setTouchable(Touchable.enabled);
		content.setTouchable(Touchable.childrenOnly);
		content.add(scrollPane).setSize(width, Gdx.graphics.getHeight() * 0.4f);
		depTable.invalidate();
		depTable.layout();

		Command saveAndPublish = new CEditorSave(mEditor, new CEditorPublish(mEditor));

		msgBox.content(content);
		msgBox.button("Publish", saveAndPublish);
		msgBox.addCancelButtonAndKeys();
		showMsgBox(msgBox);
	}

	/**
	 * Shows the confirm message box for exiting to main menu
	 */
	void showExitConfirmDialog() {
		if (mEditor.isSaved()) {
			MsgBoxExecuter msgBox = getFreeMsgBox(true);
			msgBox.setTitle("Exit to Main Menu");
			msgBox.content(Messages.Editor.EXIT_TO_MAIN_MENU);
			msgBox.button("Exit", new CSceneReturn(MainMenu.class));
			msgBox.addCancelButtonAndKeys();
			showMsgBox(msgBox);
		} else {
			switchReturnTo("Main Menu", new CSceneReturn(MainMenu.class), UnsavedActions.MAIN_MENU);
		}
	}

	/**
	 * Shows the information for the resource we're editing
	 */
	protected void showInfoDialog() {
		setInfoNameError("");
		String OPTION_DELIMITER = "option-dialog";

		mInvoker.pushDelimiter(OPTION_DELIMITER);
		MsgBoxExecuter msgBox = getFreeMsgBox(true);
		msgBox.setTitle(getResourceTypeNameCapital() + " Options");
		msgBox.content(mInfoTable);
		if (mEditor.isJustCreated()) {
			msgBox.addCancelButtonAndKeys(new CEditorUndoJustCreated(mEditor));
		} else {
			msgBox.addCancelButtonAndKeys(new CInvokerUndoToDelimiter(mInvoker, OPTION_DELIMITER, false));
		}
		Command save = new CDefHasValidName(msgBox, this, mEditor, getResourceTypeName());
		msgBox.button("Save", save);
		msgBox.key(Input.Keys.ENTER, save);
		showMsgBox(msgBox);
	}

	/**
	 * Switches or returns to the specified editor or menu.
	 * @param switchReturnTo what we're switching or returning to.
	 * @param command the command to be executed when switching or returning
	 * @param unsavedAction message to be displayed in the message box
	 */
	private void switchReturnTo(String switchReturnTo, Command command, UnsavedActions unsavedAction) {
		boolean switching = command instanceof CSceneSwitch;

		String msgBoxTitle = switching ? "Switch" : "Exit";
		msgBoxTitle += " to " + switchReturnTo;

		String saveMessage = switching ? "Save then switch" : "Save then exit";
		String justExecuteMessage = switching ? "Switch" : "Exit";

		String content = Messages.getUnsavedMessage(getResourceTypeName(), unsavedAction);

		executeCommandAndCheckSave(command, msgBoxTitle, saveMessage, justExecuteMessage, content);
	}

	/**
	 * Run a command. If the editor isn't saved a message box is displayed that asks the
	 * player to either save or discard the resource before executing the command.
	 * @param command the command to run
	 * @param title message box title
	 * @param saveButtonText text for the save button then execute the command
	 * @param withoutSaveButtonText text for the button when to just execute the command
	 *        without saving
	 * @param content the message to be displayed in the message box
	 */
	protected void executeCommandAndCheckSave(Command command, String title, String saveButtonText, String withoutSaveButtonText, String content) {
		executeCommandAndCheckSave(command, title, saveButtonText, withoutSaveButtonText, content, false);
	}

	/**
	 * Run a command. Displays a message box that asks the player to either save or
	 * discard the resources before executing the command.
	 * @param command the command to run
	 * @param title message box title
	 * @param saveButtonText text for the save button then execute the command
	 * @param withoutSaveButtonText text for the button when to just execute the command
	 *        without saving
	 * @param content the message to be displayed in the message box
	 * @param alwaysShow set to true to always show the message box even if the resource
	 *        has been saved
	 */
	protected void executeCommandAndCheckSave(Command command, String title, String saveButtonText, String withoutSaveButtonText, String content,
			boolean alwaysShow) {
		if (!mEditor.isSaved() || alwaysShow) {
			Command saveAndExecute = new CEditorSave(mEditor, command);

			MsgBoxExecuter msgBox = getFreeMsgBox(true);

			msgBox.clear();
			msgBox.setTitle(title);
			msgBox.content(content);
			msgBox.button(saveButtonText, saveAndExecute);
			msgBox.button(withoutSaveButtonText, command);
			msgBox.addCancelButtonAndKeys();
			showMsgBox(msgBox);
		} else {
			command.execute();
		}
	}

	/**
	 * Reset and add collision boxes for all UI-elements. Should be called every frame,
	 * will reset only when necessary.
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
	 * Calculate top and bottom margin
	 * @pre initStyles() have to be called before using this method
	 * @return number of pixels to pad above and below the bars
	 */
	protected float getTopBottomPadding() {
		return mUiFactory.getStyles().vars.barUpperLowerHeight + mUiFactory.getStyles().vars.paddingOuter;
	}

	/**
	 * @return resource type name
	 */
	protected abstract String getResourceTypeName();

	/**
	 * @return resource type name with capital first letters
	 */
	protected String getResourceTypeNameCapital() {
		return WordUtils.capitalize(getResourceTypeName());
	}

	/**
	 * Set the info name error label text
	 * @param errorText the error text to display
	 */
	public abstract void setInfoNameError(String errorText);


	/** Tooltip widget */
	protected TooltipWidget mTooltip = null;
	/** Invoker */
	protected Invoker mInvoker = null;
	/** UI elements that should be disabled during publish */
	protected ArrayList<Actor> mDisabledWhenPublished = new ArrayList<>();
	/** Enemy highlight button */
	private Button mEnemyHighlight = null;
	/** Grid button */
	private Button mGridRender = null;
	/** Grid above button */
	private Button mGridRenderAbove = null;
	/** Editor scene */
	protected IEditor mEditor = null;
	/** Editor menu table (upper left) */
	private AlignTable mEditorMenu = new AlignTable();
	/** File menu table (upper right) */
	private AlignTable mFileMenu = new AlignTable();
	/** Edit menu table (upper center) */
	private AlignTable mEditMenu = new AlignTable();
	/** Tool table (left) */
	protected AlignTable mToolMenu = new AlignTable();
	/** Table with the name */
	private AlignTable mNameTable = new AlignTable();
	/** Info table */
	protected AlignTable mInfoTable = new AlignTable();
	/** Name label */
	private Label mNameLabel = null;
	/**
	 * If the main table has a valid layout, false means the collision boxes will be
	 * updated once the main table has a valid layout again
	 */
	private boolean mLayoutWasValid = false;
	/** All UI-bodies for collision */
	private ArrayList<Body> mBodies = null;
	/** Setting widget */
	protected TabWidget mSettingTabs = new TabWidget();
}
