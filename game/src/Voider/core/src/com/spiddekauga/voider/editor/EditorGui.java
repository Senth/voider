package com.spiddekauga.voider.editor;

import java.util.ArrayList;
import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.spiddekauga.utils.commands.CEventConnect;
import com.spiddekauga.utils.commands.CInvokerUndoToDelimiter;
import com.spiddekauga.utils.commands.CMusicStop;
import com.spiddekauga.utils.commands.CSceneReturn;
import com.spiddekauga.utils.commands.CSceneSwitch;
import com.spiddekauga.utils.commands.CSequence;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.commands.Invoker;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.Background;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.utils.scene.ui.TabWidget;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.utils.scene.ui.TooltipWidget;
import com.spiddekauga.utils.scene.ui.TooltipWidget.ITooltip;
import com.spiddekauga.utils.scene.ui.validate.VFieldLength;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Debug.Builds;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Editor.IC_Level;
import com.spiddekauga.voider.editor.commands.CDefHasValidName;
import com.spiddekauga.voider.editor.commands.CEditorDuplicate;
import com.spiddekauga.voider.editor.commands.CEditorLoad;
import com.spiddekauga.voider.editor.commands.CEditorNew;
import com.spiddekauga.voider.editor.commands.CEditorPublish;
import com.spiddekauga.voider.editor.commands.CEditorSave;
import com.spiddekauga.voider.editor.commands.CEditorUndoJustCreated;
import com.spiddekauga.voider.editor.commands.CLevelRun;
import com.spiddekauga.voider.game.actors.ActorFilterCategories;
import com.spiddekauga.voider.menu.MainMenu;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.repo.resource.SkinNames.EditorIcons;
import com.spiddekauga.voider.repo.user.User;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.IResourceTexture;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.ui.UiFactory.BarLocations;
import com.spiddekauga.voider.scene.ui.UiStyles.LabelStyles;
import com.spiddekauga.voider.sound.MusicInterpolations;
import com.spiddekauga.voider.utils.Messages;
import com.spiddekauga.voider.utils.Messages.UnsavedActions;
import com.spiddekauga.voider.utils.commands.CUserConnect;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;

/**
 * Common methods for all editors
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class EditorGui extends Gui {
	@Override
	public void dispose() {
		mEditorMenu.dispose();
		mFileMenu.dispose();
		mToolMenu.dispose();
		mEditMenu.dispose();
		mNameTable.dispose();

		if (mSettingTabs != null) {
			mSettingTabs.remove();
		}
		if (mTooltip != null) {
			mTooltip.remove();
		}

		if (mBodies != null) {
			clearCollisionBoxes();
		}

		// Remove top & bottom bar
		ArrayList<Actor> barsToRemove = new ArrayList<>();
		for (Actor actor : getStage().getActors()) {
			if (actor instanceof Background) {
				barsToRemove.add(actor);
			}
		}
		for (Actor removeActor : barsToRemove) {
			removeActor.remove();
		}

		super.dispose();
	}

	/**
	 * Sets the editor bound to this GUI
	 * @param editor the editor bound to this GUI
	 */
	protected void setEditor(Editor editor) {
		mEditor = editor;
		mInvoker = mEditor.getInvoker();
	}

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
		}

		initTooltipBar();

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
		mSettingTabs = mUiFactory.createRightPanel();
		getStage().addActor(mSettingTabs);
	}

	/**
	 * Initializes the top bar and bottom bar
	 */
	private void initTopBottomBar() {
		mUiFactory.addBar(BarLocations.TOP_BOTTOM, true, getStage());
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

		if (mGridRender != null) {
			mGridRender.setChecked(mEditor.isGridOn());
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
		MsgBoxExecuter msgBox = mUiFactory.msgBox.add(null);

		// New
		msgBox.button("New", new CEditorNew(mEditor));
		msgBox.buttonRow();
		msgBox.button("Load", new CEditorLoad(mEditor));
		msgBox.getButtonCell().setPadBottom(mUiFactory.getStyles().vars.paddingInner);

		// Editors
		if (this.getClass() != LevelEditorGui.class) {
			msgBox.buttonRow();
			msgBox.button("Level Editor", new CSceneSwitch(LevelEditor.class));
		}
		if (this.getClass() != EnemyEditorGui.class) {
			msgBox.buttonRow();
			msgBox.button("Enemy Editor", new CSceneSwitch(EnemyEditor.class));
		}
		if (this.getClass() != BulletEditorGui.class) {
			msgBox.buttonRow();
			msgBox.button("Bullet Editor", new CSceneSwitch(BulletEditor.class));
		}


		// Back
		msgBox.getButtonCell().setPadBottom(mUiFactory.getStyles().vars.paddingInner);
		msgBox.buttonRow();
		msgBox.addCancelButtonAndKeys("Main Menu", new CSceneReturn(MainMenu.class));
	}

	/**
	 * Initializes the editor menu
	 */
	private void initEditorMenu() {
		Button button;

		// Level editor
		if (this.getClass() == LevelEditorGui.class) {
			button = mUiFactory.button.addImage(EditorIcons.LEVEL_EDITOR_SELECTED, mEditorMenu, null, null);
		} else {
			button = mUiFactory.button.addImage(EditorIcons.LEVEL_EDITOR, mEditorMenu, null, null);
		}
		if (this.getClass() != LevelEditorGui.class) {
			new ButtonListener(button) {
				@Override
				protected void onPressed(Button button) {
					switchReturnTo("Level Editor", new CSceneSwitch(LevelEditor.class), UnsavedActions.LEVEL_EDITOR);
				}
			};
		}
		mTooltip.add(button, Messages.EditorTooltips.EDITOR_LEVEL);


		// Enemy editor
		if (this.getClass() == EnemyEditorGui.class) {
			button = mUiFactory.button.addImage(EditorIcons.ENEMY_EDITOR_SELECTED, mEditorMenu, null, null);
		} else {
			button = mUiFactory.button.addImage(EditorIcons.ENEMY_EDITOR, mEditorMenu, null, null);
		}
		if (this.getClass() != EnemyEditorGui.class) {
			new ButtonListener(button) {
				@Override
				protected void onPressed(Button button) {
					switchReturnTo("Enemy Editor", new CSceneSwitch(EnemyEditor.class), UnsavedActions.ENEMY_EDITOR);
				}
			};
		}
		mTooltip.add(button, Messages.EditorTooltips.EDITOR_ENEMY);


		// Bullet editor
		if (this.getClass() == BulletEditorGui.class) {
			button = mUiFactory.button.addImage(EditorIcons.BULLET_EDITOR_SELECTED, mEditorMenu, null, null);
		} else {
			button = mUiFactory.button.addImage(EditorIcons.BULLET_EDITOR, mEditorMenu, null, null);
		}
		if (this.getClass() != BulletEditorGui.class) {
			new ButtonListener(button) {
				@Override
				protected void onPressed(Button button) {
					switchReturnTo("Bullet Editor", new CSceneSwitch(BulletEditor.class), UnsavedActions.BULLET_EDITOR);
				}
			};
		}
		mTooltip.add(button, Messages.EditorTooltips.EDITOR_BULLET);


		// Ship editor
		if (Config.Debug.isBuildOrBelow(Builds.NIGHTLY_RELEASE)) {
			if (this.getClass() == ShipEditorGui.class) {
				button = mUiFactory.button.addImage(EditorIcons.SHIP_EDITOR_SELECTED, mEditorMenu, null, null);
			} else {
				button = mUiFactory.button.addImage(EditorIcons.SHIP_EDITOR, mEditorMenu, null, null);
			}
			if (this.getClass() != ShipEditorGui.class) {
				new ButtonListener(button) {
					@Override
					protected void onPressed(Button button) {
						switchReturnTo("Ship Editor", new CSceneSwitch(ShipEditor.class), UnsavedActions.SHIP_EDITOR);
					}
				};
			}
			mTooltip.add(button, Messages.EditorTooltips.EDITOR_SHIP);
		}
	}

	/**
	 * Initializes the edit menu
	 */
	private void initEditMenu() {
		Button button;

		// Undo
		button = mUiFactory.button.addImage(EditorIcons.UNDO, mEditMenu, null, mDisabledWhenPublished);
		mTooltip.add(button, Messages.EditorTooltips.ACTION_UNDO);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mEditor.getInvoker().undo();
			}
		};

		// Redo
		button = mUiFactory.button.addImage(EditorIcons.REDO, mEditMenu, null, mDisabledWhenPublished);
		mTooltip.add(button, Messages.EditorTooltips.ACTION_REDO);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mEditor.getInvoker().redo();
			}
		};

		// Bug Report
		button = mUiFactory.button.addImage(SkinNames.General.PANEL_BUG, mEditMenu, null, null);
		mTooltip.add(button, Messages.EditorTooltips.ACTION_BUG_REPORT);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mUiFactory.msgBox.bugReport();
			}
		};

		// Grid stuff
		if (!(mEditor instanceof CampaignEditor)) {
			// Grid
			button = mUiFactory.button.addImage(EditorIcons.GRID, mEditMenu, null, null);
			mTooltip.add(button, Messages.EditorTooltips.ACTION_GRID_TOGGLE);
			mGridRender = button;
			new ButtonListener(button) {
				@Override
				protected void onChecked(Button button, boolean checked) {
					mEditor.setGrid(checked);
				}
			};
		}

		// Level Editor -> Run & Enemy highlight
		if (mEditor instanceof LevelEditor) {
			// Run from start
			button = mUiFactory.button.addImage(EditorIcons.RUN_FROM_START, mEditMenu, null, null);
			mTooltip.add(button, Messages.EditorTooltips.ACTION_PLAY_FROM_START);
			new ButtonListener(button) {
				@Override
				protected void onPressed(Button button) {
					MsgBoxExecuter msgBox = mUiFactory.msgBox.add(Messages.Level.RUN_INVULNERABLE_TITLE);

					msgBox.content(Messages.Level.RUN_INVULNERABLE_CONTENT);
					msgBox.addCancelButtonAndKeys();
					msgBox.button("Can die", new CLevelRun(false, true, (LevelEditor) mEditor));
					msgBox.button("Invulnerable", new CLevelRun(true, true, (LevelEditor) mEditor));
				}
			};

			// Run from here
			button = mUiFactory.button.addImage(EditorIcons.RUN, mEditMenu, null, null);
			mTooltip.add(button, Messages.EditorTooltips.ACTION_PLAY_FROM_HERE);
			new ButtonListener(button) {
				@Override
				protected void onPressed(Button button) {
					MsgBoxExecuter msgBox = mUiFactory.msgBox.add(Messages.Level.RUN_INVULNERABLE_TITLE);

					msgBox.content(Messages.Level.RUN_INVULNERABLE_CONTENT);
					msgBox.addCancelButtonAndKeys();
					msgBox.button("Can die", new CLevelRun(false, false, (LevelEditor) mEditor));
					msgBox.button("Invulnerable", new CLevelRun(true, false, (LevelEditor) mEditor));
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
		button = mUiFactory.button.addImage(EditorIcons.NEW, mFileMenu, null, null);
		ITooltip tooltip = getFileNewTooltip();
		if (tooltip != null) {
			mTooltip.add(button, tooltip);
		}
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				executeCommandAndCheckSave(new CEditorNew(mEditor), "New " + getResourceTypeName(), "Save first", "Discard current",
						Messages.getUnsavedMessage(getResourceTypeName(), UnsavedActions.NEW));
			}
		};

		// Duplicate
		button = mUiFactory.button.addImage(EditorIcons.DUPLICATE, mFileMenu, null, null);
		tooltip = getFileDuplicateTooltip();
		if (tooltip != null) {
			mTooltip.add(button, tooltip);
		}
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mEditor.duplicateDef();
			}
		};

		// Save
		button = mUiFactory.button.addImage(EditorIcons.SAVE, mFileMenu, null, mDisabledWhenPublished);
		mTooltip.add(button, Messages.EditorTooltips.FILE_SAVE);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mEditor.saveDef();
			}
		};

		// Load
		button = mUiFactory.button.addImage(EditorIcons.LOAD, mFileMenu, null, null);
		mTooltip.add(button, Messages.EditorTooltips.FILE_OPEN);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				open();
			}
		};

		// Publish
		if (!(this instanceof ShipEditorGui)) {
			button = mUiFactory.button.addImage(EditorIcons.PUBLISH, mFileMenu, null, mDisabledWhenPublished);
			tooltip = getFilePublishTooltip();
			if (tooltip != null) {
				mTooltip.add(button, tooltip);
			}
			new ButtonListener(button) {
				@Override
				protected void onPressed(Button button) {
					boolean showPublish = true;

					// For level, make sure level has screen shot before publishing
					if (mEditor instanceof LevelEditor) {
						if (!((LevelEditor) mEditor).hasScreenshot()) {
							showPublish = false;

							MsgBoxExecuter msgBox = mUiFactory.msgBox.add("No Screenshot Taken");
							String text = "Please take a screenshot of this level before publishing it. "
									+ "You can do this by test running the level and click on the camera " + "icon in the top bar.";
							Label label = mUiFactory.text.create(text, true);
							msgBox.content(label);
							msgBox.addCancelButtonAndKeys("OK");
							label.setWidth(Gdx.graphics.getWidth() * 0.5f);
						}

						float levelLength = ((LevelEditor) mEditor).getLevel().getLevelDef().getLengthInTime();
						IC_Level icLevel = ConfigIni.getInstance().editor.level;
						if (showPublish && levelLength < icLevel.getPublishLengthMin()) {
							showPublish = false;

							MsgBoxExecuter msgBox = mUiFactory.msgBox.add("Level Too Short");
							String text = "Please add more content to your level. Your current level has a length of " + ((int) levelLength)
									+ " seconds, minimum is 30 seconds.";
							msgBox.content(text).setActorWidth(Gdx.graphics.getWidth() * 0.5f);

							msgBox.addCancelButtonAndKeys("OK");
						}
					}

					// Check if online
					if (showPublish && !User.getGlobalUser().isOnline()) {
						showPublish = false;

						MsgBoxExecuter msgBox = mUiFactory.msgBox.add("Go Online?");
						String text = "You need to go online to publish the " + getResourceTypeName() + ".";
						Label label = mUiFactory.text.create(text, false);

						final IEventListener loginListener = new IEventListener() {
							@Override
							public void handleEvent(GameEvent event) {
								EventDispatcher.getInstance().disconnect(EventTypes.USER_CONNECTED, this);
								showPublishDialog();
							}
						};
						Command connectEvents = new CEventConnect(loginListener, EventTypes.USER_CONNECTED);
						Command goOnline = new CUserConnect();

						msgBox.content(label);
						msgBox.addCancelButtonAndKeys();
						msgBox.button("Connect & Publish", new CSequence(connectEvents, goOnline));
					}

					if (showPublish) {
						showPublishDialog();
					}
				}
			};
		}

		// Info
		button = mUiFactory.button.addImage(EditorIcons.INFO, mFileMenu, null, null);
		tooltip = getFileInfoTooltip();
		if (tooltip != null) {
			mTooltip.add(button, tooltip);
		}
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				showInfoDialog();
			}
		};
	}

	/**
	 * Open/Load another definition
	 */
	void open() {
		executeCommandAndCheckSave(new CEditorLoad(mEditor), "Load another " + getResourceTypeName(), "Save first", "Discard current",
				Messages.getUnsavedMessage(getResourceTypeName(), UnsavedActions.LOAD));
	}

	/**
	 * Shows the publish message box
	 */
	private void showPublishDialog() {
		MsgBoxExecuter msgBox = mUiFactory.msgBox.add("Publish");

		AlignTable content = new AlignTable();
		content.setAlign(Horizontal.CENTER, Vertical.MIDDLE);
		float width = Gdx.graphics.getWidth() * 0.5f;
		float imagePad = mUiFactory.getStyles().vars.paddingSeparator;

		// Publish text
		Label label = mUiFactory.text.add("You are about to publish your " + getResourceTypeName() + " '" + mEditor.getName() + "' online.", true,
				content, LabelStyles.HIGHLIGHT);
		label.setAlignment(Align.center);
		content.getCell().setWidth(width);

		// Irreversible text
		mUiFactory.text.addParagraphRow(content);
		label = mUiFactory.text.add("This is irreversible and once publish the assets cannot be edited or removed.", content, LabelStyles.ERROR);
		label.setAlignment(Align.center);

		// More info
		mUiFactory.text.addParagraphRow(content);
		label = mUiFactory.text.add("All dependencies will also be published. Below is a list of everything that will be published.", true, content);
		label.setAlignment(Align.center);
		content.getCell().setWidth(width);


		// Add all resources that will be published
		mUiFactory.text.addParagraphRow(content);
		AlignTable depTable = new AlignTable();
		depTable.setAlignTable(Horizontal.LEFT, Vertical.TOP);
		depTable.setAlignRow(Horizontal.LEFT, Vertical.MIDDLE);

		ArrayList<Def> dependencies = mEditor.getNonPublishedDependencies();
		for (Def dependency : dependencies) {
			mUiFactory.text.addParagraphRow(depTable).setPadLeft(mUiFactory.getStyles().vars.paddingInner);

			// Add image
			if (dependency instanceof IResourceTexture) {
				Image image = new Image(((IResourceTexture) dependency).getTextureRegionDrawable());
				depTable.add(image).setHeight(100).setKeepAspectRatio(true);
			} else {
				depTable.add().setPadRight(100);
			}

			// Add name
			mUiFactory.text.add(dependency.getName(), depTable, LabelStyles.PUBLISH_NAME);
			depTable.getCell().setPadLeft(imagePad);
		}

		// Remove padding from first row
		if (depTable.getRowCount() > 0) {
			depTable.getRows().get(0).setPadTop(0);
		}


		ScrollPane scrollPane = new ScrollPane(depTable, mUiFactory.getStyles().scrollPane.windowBackground);
		scrollPane.setTouchable(Touchable.enabled);
		content.setTouchable(Touchable.childrenOnly);
		content.add(scrollPane).setSize(width, Gdx.graphics.getHeight() * 0.4f);
		depTable.invalidate();
		depTable.layout();

		Command saveAndPublish = new CEditorSave(mEditor, new CEditorPublish(mEditor));

		msgBox.content(content);
		msgBox.addCancelButtonAndKeys();
		msgBox.button("Publish", saveAndPublish);
	}

	/**
	 * Shows the confirm message box for exiting to main menu
	 */
	void showExitConfirmDialog() {
		if (mEditor.isSaved()) {
			MsgBoxExecuter msgBox = mUiFactory.msgBox.add("Exit to Main Menu");
			msgBox.content(Messages.Editor.EXIT_TO_MAIN_MENU);
			msgBox.addCancelButtonAndKeys();
			msgBox.button("Exit", new CSceneReturn(MainMenu.class));
		} else {
			switchReturnTo("Main Menu", new CSceneReturn(MainMenu.class), UnsavedActions.MAIN_MENU);
		}
	}

	/**
	 * Resets the info. This is called before the info dialog is shown
	 */
	protected void resetInfo() {
		// Does nothing
	}

	/**
	 * Shows the information for the resource we're editing
	 */
	protected void showInfoDialog() {
		resetInfo();
		setInfoNameError("");
		String OPTION_DELIMITER = "option-dialog";

		mInvoker.pushDelimiter(OPTION_DELIMITER);
		MsgBoxExecuter msgBox = mUiFactory.msgBox.add(getResourceTypeNameCapital() + " Options");
		msgBox.content(mInfoTable);
		if (mEditor.isJustCreated()) {
			msgBox.addCancelButtonAndKeys(new CEditorUndoJustCreated(mEditor));
		} else {
			// Stop the music
			Command cancelCommand = null;
			Command undoToDelimiter = new CInvokerUndoToDelimiter(mInvoker, OPTION_DELIMITER, false);
			if (this instanceof LevelEditorGui) {
				cancelCommand = new CSequence(undoToDelimiter, new CMusicStop(MusicInterpolations.FADE_OUT));
			} else {
				cancelCommand = undoToDelimiter;
			}
			msgBox.addCancelButtonAndKeys(cancelCommand);
		}
		Command saveValidate = new CDefHasValidName(msgBox, this, mEditor, getResourceTypeName());
		Command save = null;
		if (this instanceof LevelEditorGui) {
			save = new CSequence(saveValidate, new CMusicStop(MusicInterpolations.FADE_OUT));
		} else {
			save = saveValidate;
		}
		msgBox.button("Save", save);
	}

	/**
	 * Show duplicate information message box
	 */
	protected void showDuplicateDialog() {
		AlignTable table = new AlignTable();
		table.setAlignTable(Horizontal.LEFT, Vertical.TOP);
		table.setAlignRow(Horizontal.LEFT, Vertical.MIDDLE);
		table.setPad(mUiFactory.getStyles().vars.paddingInner);
		TextFieldListener listener;

		String oldName = mEditor.getName();
		String oldDescription = mEditor.getDescription();

		final CEditorDuplicate editorDuplicate = new CEditorDuplicate(mEditor, oldName, oldDescription);

		// Name
		listener = new TextFieldListener(mInvoker) {
			@Override
			protected void onChange(String newText) {
				editorDuplicate.setName(newText);
			}
		};
		TextField name = mUiFactory.addTextField("New Name", true, "New name", listener, table, null);
		name.setMaxLength(ConfigIni.getInstance().editor.general.getNameLengthMax());
		name.setText(oldName);
		Label nameError = mUiFactory.text.getLastCreatedErrorLabel();
		VFieldLength validateNameLength = new VFieldLength(listener, nameError, Config.Actor.NAME_LENGTH_MIN);


		// Description
		listener = new TextFieldListener(mInvoker) {
			@Override
			protected void onChange(String newText) {
				editorDuplicate.setDescription(newText);
			}
		};
		TextField description = mUiFactory.addTextArea("Description", false, null, listener, table, null);
		description.setText(oldDescription);
		description.setMaxLength(ConfigIni.getInstance().editor.general.getDescriptionLengthMax());


		MsgBoxExecuter msgBox = mUiFactory.msgBox.add("Set copy information");
		msgBox.content(table);
		msgBox.addCancelButtonAndKeys();
		msgBox.button("Create Copy", editorDuplicate, validateNameLength);
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

			MsgBoxExecuter msgBox = mUiFactory.msgBox.add(title);
			msgBox.content(content);
			msgBox.addCancelButtonAndKeys();
			msgBox.button(saveButtonText, saveAndExecute);
			msgBox.button(withoutSaveButtonText, command);
		} else {
			command.execute();
		}
	}

	/**
	 * Create collision boxes for these actors
	 * @param actors
	 */
	void createCollisionBoxes(Actor... actors) {
		Vector2 localPos = new Vector2();
		for (Actor actor : actors) {
			if (actor.isVisible()) {
				if (actor instanceof Layout) {
					((Layout) actor).validate();
				}
				localPos.set(0, 0);
				Vector2 screenPos = actor.localToStageCoordinates(localPos);
				createCollisionBox(screenPos.x, screenPos.y, actor.getWidth(), actor.getHeight());
			}
		}
	}

	/**
	 * Create a custom collision box
	 * @param x screen position
	 * @param y screen position
	 * @param width width in screen coordinates
	 * @param height height in screen coordinates
	 */
	void createCollisionBox(float x, float y, float width, float height) {
		if (mEditor.getCamera() != null) {
			float scale = mEditor.getScreenToWorldScale() * 0.5f;

			float worldWidth = width * scale;
			float worldHeight = height * scale;

			mCollisionBoxVars.shape.setAsBox(worldWidth, worldHeight);

			// Convert screen to world coordinates
			Scene.screenToWorldCoord(mEditor.getCamera(), x + width * 0.5f, Gdx.graphics.getHeight() - (y + height * 0.5f),
					mCollisionBoxVars.bodyDef.position, false);

			// Create body
			Body body = mEditor.getWorld().createBody(mCollisionBoxVars.bodyDef);
			body.createFixture(mCollisionBoxVars.fixtureDef);
			mBodies.add(body);
		}
	}

	/**
	 * Reset and add collision boxes for all UI-elements. Should be called once initially
	 * and when
	 */
	void resetCollisionBoxes() {
		if (mEditor == null) {
			return;
		}

		clearCollisionBoxes();
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
		String name = getResourceTypeName();
		return name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1);
	}

	/**
	 * Set the info name error label text
	 * @param errorText the error text to display
	 */
	public abstract void setInfoNameError(String errorText);


	/**
	 * Container class for collision box standard variables
	 */
	private static class CollisionBoxVars {
		BodyDef bodyDef = new BodyDef();
		PolygonShape shape = new PolygonShape();
		FixtureDef fixtureDef = new FixtureDef();

		{
			bodyDef.type = BodyType.StaticBody;
			fixtureDef.filter.categoryBits = ActorFilterCategories.SCREEN_BORDER;
			fixtureDef.filter.maskBits = ActorFilterCategories.PLAYER;
			fixtureDef.shape = shape;
		}
	}

	private CollisionBoxVars mCollisionBoxVars = new CollisionBoxVars();
	/** Tooltip widget */
	protected TooltipWidget mTooltip = null;
	/** Invoker */
	protected Invoker mInvoker = null;
	/** UI elements that should be disabled during publish */
	protected ArrayList<Actor> mDisabledWhenPublished = new ArrayList<>();
	/** Grid button */
	private Button mGridRender = null;
	/** Editor scene */
	protected Editor mEditor = null;
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
	/** All UI-bodies for collision */
	private ArrayList<Body> mBodies = new ArrayList<>();
	/** Setting widget */
	protected TabWidget mSettingTabs = null;
}
