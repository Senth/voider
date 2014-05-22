package com.spiddekauga.voider.editor;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
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
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.Background;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.DisableListener;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.utils.scene.ui.TooltipListener;
import com.spiddekauga.utils.scene.ui.UiPanelFactory;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.editor.commands.CEditorDuplicate;
import com.spiddekauga.voider.editor.commands.CEditorLoad;
import com.spiddekauga.voider.editor.commands.CEditorNew;
import com.spiddekauga.voider.editor.commands.CEditorPublish;
import com.spiddekauga.voider.editor.commands.CEditorSave;
import com.spiddekauga.voider.editor.commands.CLevelRun;
import com.spiddekauga.voider.editor.commands.CSceneReturn;
import com.spiddekauga.voider.editor.commands.CSceneSwitch;
import com.spiddekauga.voider.game.actors.ActorFilterCategories;
import com.spiddekauga.voider.menu.MainMenu;
import com.spiddekauga.voider.repo.InternalNames;
import com.spiddekauga.voider.repo.ResourceCacheFacade;
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
 * 
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

		mUiFactory.init();

		if (mEditorMenu.getStage() == null) {
			getStage().addActor(mEditorMenu);
			getStage().addActor(mFileMenu);
			getStage().addActor(mToolMenu);
		}

		mBodies = Pools.arrayList.obtain();

		initStyles();

		mEditorMenu.setAlignTable(Horizontal.LEFT, Vertical.TOP);
		mFileMenu.setAlignTable(Horizontal.RIGHT, Vertical.TOP);
		mToolMenu.setAlign(Horizontal.LEFT, Vertical.TOP);
		mMainTable.setAlignTable(Horizontal.RIGHT, Vertical.TOP);
		mMainTable.setAlignRow(Horizontal.RIGHT, Vertical.MIDDLE);

		initEditorMenu();
		initFileMenu();

		float paddingOuter = mStyles.vars.paddingOuter;
		mToolMenu.setMargin(getTopBottomPadding(), paddingOuter, getTopBottomPadding(), paddingOuter);

		initTopBar();
	}

	/**
	 * Initializes the styles
	 */
	private void initStyles() {
		mStyles.skin.general = ResourceCacheFacade.get(InternalNames.UI_GENERAL);
		mStyles.skin.editor = ResourceCacheFacade.get(InternalNames.UI_EDITOR);
		mStyles.textButton.press = SkinNames.getResource(SkinNames.General.TEXT_BUTTON_PRESS);
		mStyles.textButton.toggle = SkinNames.getResource(SkinNames.General.TEXT_BUTTON_TOGGLE);
		mStyles.textButton.selected = SkinNames.getResource(SkinNames.General.TEXT_BUTTON_SELECTED);
		mStyles.slider.standard = SkinNames.getResource(SkinNames.General.SLIDER_DEFAULT);
		mStyles.textField.standard = SkinNames.getResource(SkinNames.General.TEXT_FIELD_DEFAULT);
		mStyles.label.standard = SkinNames.getResource(SkinNames.General.LABEL_DEFAULT);
		mStyles.label.error = SkinNames.getResource(SkinNames.General.LABEL_ERROR);
		mStyles.label.highlight = SkinNames.getResource(SkinNames.General.LABEL_HIGHLIGHT);
		mStyles.label.success = SkinNames.getResource(SkinNames.General.LABEL_SUCCESS);
		mStyles.checkBox.checkBox = SkinNames.getResource(SkinNames.General.CHECK_BOX_DEFAULT);
		mStyles.checkBox.radio = SkinNames.getResource(SkinNames.General.CHECK_BOX_RADIO);
		mStyles.scrollPane.noBackground = SkinNames.getResource(SkinNames.General.SCROLL_PANE_DEFAULT);
		mStyles.scrollPane.windowBackground = SkinNames.getResource(SkinNames.General.SCROLL_PANE_WINDOW_BACKGROUND);

		// Colors
		mStyles.colors.widgetBackground = SkinNames.getResource(SkinNames.GeneralVars.WIDGET_BACKGROUND_COLOR);

		// Vars
		mStyles.vars.paddingDefault = SkinNames.getResource(SkinNames.GeneralVars.PADDING_DEFAULT);
		mStyles.vars.paddingSeparator = SkinNames.getResource(SkinNames.GeneralVars.PADDING_SEPARATOR);
		mStyles.vars.paddingAfterLabel = SkinNames.getResource(SkinNames.GeneralVars.PADDING_AFTER_LABEL);
		mStyles.vars.paddingOuter = SkinNames.getResource(SkinNames.GeneralVars.PADDING_OUTER);
		mStyles.vars.paddingInner = SkinNames.getResource(SkinNames.GeneralVars.PADDING_INNER);
		mStyles.vars.textFieldNumberWidth = SkinNames.getResource(SkinNames.GeneralVars.TEXT_FIELD_NUMBER_WIDTH);
		mStyles.vars.barUpperLowerHeight = SkinNames.getResource(SkinNames.GeneralVars.BAR_UPPER_LOWER_HEIGHT);
	}

	/**
	 * Initializes the top bar
	 */
	private void initTopBar() {
		mTopBar = new Background(mStyles.colors.widgetBackground);
		mTopBar.setSize(Gdx.graphics.getWidth(), mStyles.vars.barUpperLowerHeight);
		mTopBar.setPosition(0, Gdx.graphics.getHeight() - mStyles.vars.barUpperLowerHeight);
		getStage().addActor(mTopBar);
		mTopBar.setZIndex(0);
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
			button = new ImageButton(mStyles.skin.editor, EditorIcons.CAMPAIGN_EDITOR_SELECTED.toString());
		} else {
			button = new ImageButton(mStyles.skin.editor, EditorIcons.CAMPAIGN_EDITOR.toString());
		}
		TooltipListener tooltipListener = new TooltipListener(button, Messages.replaceName(Messages.Tooltip.Menus.Editor.CAMPAIGN,  "level"));
		mEditorMenu.add(button);
		if (this.getClass() != LevelEditorGui.class) {
			new ButtonListener(button, tooltipListener) {
				@Override
				protected void onPressed() {
					switchReturnTo("Campaign Editor", new CSceneSwitch(CampaignEditor.class), UnsavedActions.CAMPAIGN_EDITOR);
				}
			};
		}


		// Level editor
		if (this.getClass() == LevelEditorGui.class) {
			button = new ImageButton(mStyles.skin.editor, EditorIcons.LEVEL_EDITOR_SELECTED.toString());
		} else {
			button = new ImageButton(mStyles.skin.editor, EditorIcons.LEVEL_EDITOR.toString());
		}
		tooltipListener = new TooltipListener(button, Messages.replaceName(Messages.Tooltip.Menus.Editor.LEVEL,  "level"));
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
		if (this.getClass() == EnemyEditorGui.class) {
			button = new ImageButton(mStyles.skin.editor, EditorIcons.ENEMY_EDITOR_SELECTED.toString());
		} else {
			button = new ImageButton(mStyles.skin.editor, EditorIcons.ENEMY_EDITOR.toString());
		}
		tooltipListener = new TooltipListener(button, Messages.replaceName(Messages.Tooltip.Menus.Editor.ENEMY,  "enemy"));
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
		if (this.getClass() == BulletEditorGui.class) {
			button = new ImageButton(mStyles.skin.editor, EditorIcons.BULLET_EDITOR_SELECTED.toString());
		} else {
			button = new ImageButton(mStyles.skin.editor, EditorIcons.BULLET_EDITOR.toString());
		}
		tooltipListener = new TooltipListener(button, Messages.replaceName(Messages.Tooltip.Menus.Editor.BULLET,  "bullet"));
		mEditorMenu.add(button).setPadRight(mStyles.vars.paddingSeparator);
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
	 * Initializes the file menu
	 */
	protected void initFileMenu() {
		Button button;

		// Undo
		button = new ImageButton(mStyles.skin.editor, EditorIcons.UNDO.toString());
		mDisabledWhenPublished.add(button);
		mFileMenu.add(button);
		TooltipListener tooltipListener = new TooltipListener(button, Messages.replaceName(Messages.Tooltip.Menus.File.UNDO, getResourceTypeName()));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onPressed() {
				mEditor.getInvoker().undo();
			}
		};

		// Redo
		button = new ImageButton(mStyles.skin.editor, EditorIcons.REDO.toString());
		mDisabledWhenPublished.add(button);
		mFileMenu.add(button);
		tooltipListener = new TooltipListener(button, Messages.replaceName(Messages.Tooltip.Menus.File.REDO, getResourceTypeName()));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onPressed() {
				mEditor.getInvoker().redo();
			}
		};

		// Run & Enemy highlight (for level editor)
		if (mEditor instanceof LevelEditor) {
			// Highlight enemy if it will spawn when test running the level from
			// the current position
			button = new ImageButton(mStyles.skin.editor, EditorIcons.ENEMY_SPAWN_HIGHLIGHT.toString());
			mEnemyHighlight = button;
			mFileMenu.add(button);
			tooltipListener = new TooltipListener(button, Messages.Tooltip.Menus.File.HIGHLIGHT_ENEMY);
			new ButtonListener(button, tooltipListener) {
				@Override
				protected void onChecked(boolean checked) {
					((LevelEditor) mEditor).setEnemyHighlight(checked);
				}
			};

			// Run
			button = new ImageButton(mStyles.skin.editor, EditorIcons.RUN.toString());
			mFileMenu.add(button);
			tooltipListener = new TooltipListener(button, Messages.replaceName(Messages.Tooltip.Menus.File.RUN, getResourceTypeName()));
			new ButtonListener(button, tooltipListener) {
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

		// Grid stuff
		if (getClass() != CampaignEditorGui.class) {
			// Grid
			button = new ImageButton(mStyles.skin.editor, EditorIcons.GRID.toString());
			mGridRender = button;
			DisableListener disableListener = new DisableListener(button);
			mFileMenu.add(button);
			tooltipListener = new TooltipListener(button, Messages.replaceName(Messages.Tooltip.Menus.File.GRID, getResourceTypeName()));
			new ButtonListener(button, tooltipListener) {
				@Override
				protected void onChecked(boolean checked) {
					mEditor.setGrid(checked);
				}
			};

			// Grid above
			button = new ImageButton(mStyles.skin.editor, EditorIcons.GRID_ABOVE.toString());
			mGridRenderAbove = button;
			disableListener.addToggleActor(button);
			mFileMenu.add(button).setPadRight(mStyles.vars.paddingSeparator);
			tooltipListener = new TooltipListener(button, Messages.replaceName(Messages.Tooltip.Menus.File.GRID_ADOVE, getResourceTypeName()));
			new ButtonListener(button, tooltipListener) {
				@Override
				protected void onChecked(boolean checked) {
					mEditor.setGridRenderAboveResources(checked);
				}
			};
		}

		// New
		button = new ImageButton(mStyles.skin.editor, EditorIcons.NEW.toString());
		mFileMenu.add(button);
		tooltipListener = new TooltipListener(button, Messages.replaceName(Messages.Tooltip.Menus.File.NEW, getResourceTypeName()));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onPressed() {
				executeCommandAndCheckSave(new CEditorNew(mEditor), "New " + getResourceTypeName(), "Save first", "Discard current", Messages.getUnsavedMessage(getResourceTypeName(), UnsavedActions.NEW));
			}
		};

		// Duplicate
		button = new ImageButton(mStyles.skin.editor, EditorIcons.DUPLICATE.toString());
		mFileMenu.add(button);
		tooltipListener = new TooltipListener(button, Messages.replaceName(Messages.Tooltip.Menus.File.DUPLICATE, getResourceTypeName()));
		new ButtonListener(button, tooltipListener) {
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
		button = new ImageButton(mStyles.skin.editor, EditorIcons.SAVE.toString());
		mDisabledWhenPublished.add(button);
		mFileMenu.add(button);
		tooltipListener = new TooltipListener(button, Messages.replaceName(Messages.Tooltip.Menus.File.SAVE, getResourceTypeName()));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onPressed() {
				mEditor.saveDef();
			}
		};

		// Load
		button = new ImageButton(mStyles.skin.editor, EditorIcons.LOAD.toString());
		mFileMenu.add(button);
		tooltipListener = new TooltipListener(button, Messages.replaceName(Messages.Tooltip.Menus.File.LOAD, getResourceTypeName()));
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onPressed() {
				executeCommandAndCheckSave(new CEditorLoad(mEditor), "Load another " + getResourceTypeName(), "Save first", "Discard current", Messages.getUnsavedMessage(getResourceTypeName(), UnsavedActions.LOAD));
			}
		};

		// Publish
		button = new ImageButton(mStyles.skin.editor, EditorIcons.PUBLISH.toString());
		mDisabledWhenPublished.add(button);
		mFileMenu.add(button);
		tooltipListener = new TooltipListener(button, Messages.replaceName(Messages.Tooltip.Menus.File.PUBLISH, getResourceTypeName()));
		new ButtonListener(button, tooltipListener) {
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
								+ "You can do this by test running the level and click on the camera "
								+ "icon in the top bar.";
						Label label = new Label(text, mStyles.label.standard);
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
							+ "only possible by either logging out and logging in or restarting "
							+ "the game.";
					Label label = new Label(text, mStyles.label.standard);
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
		button = new ImageButton(mStyles.skin.editor, EditorIcons.INFO.toString());
		mFileMenu.add(button);
		tooltipListener = new TooltipListener(button, Messages.replaceName(Messages.Tooltip.Menus.File.INFO, getResourceTypeName()));
		new ButtonListener(button, tooltipListener) {
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
		content.setPaddingCellDefault(mStyles.vars.paddingDefault);

		Label label = new Label("", mStyles.label.highlight);

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
		depTable.setPaddingCellDefault(mStyles.vars.paddingDefault);
		depTable.setAlign(Horizontal.LEFT, Vertical.TOP);
		ArrayList<Def> dependencies = mEditor.getNonPublishedDependencies();

		for (Def dependency : dependencies) {
			depTable.row();

			// Add image
			if (dependency instanceof IResourceTexture) {
				Image image = new Image(((IResourceTexture) dependency).getTextureRegionDrawable());
				depTable.add(image).setSize(50, 50).setPadRight(mStyles.vars.paddingAfterLabel);
			} else {
				depTable.add().setPadRight(50 + mStyles.vars.paddingAfterLabel);
			}

			// Add name
			label = new Label(dependency.getName(), mStyles.label.standard);
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
	protected abstract void showInfoDialog();

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
	 * Run a command. If the editor isn't saved a message box is displayed that asks the player
	 * to either save or discard the resource before executing the command.
	 * @param command the command to run
	 * @param title message box title
	 * @param saveButtonText text for the save button then execute the command
	 * @param withoutSaveButtonText text for the button when to just execute the command without saving
	 * @param content the message to be displayed in the message box
	 */
	protected void executeCommandAndCheckSave(Command command, String title, String saveButtonText, String withoutSaveButtonText, String content) {
		executeCommandAndCheckSave(command, title, saveButtonText, withoutSaveButtonText, content, false);
	}

	/**
	 * Run a command. Displays a message box that asks the player to either save or discard the resources
	 * before executing the command.
	 * @param command the command to run
	 * @param title message box title
	 * @param saveButtonText text for the save button then execute the command
	 * @param withoutSaveButtonText text for the button when to just execute the command without saving
	 * @param content the message to be displayed in the message box
	 * @param alwaysShow set to true to always show the message box even if the resource has been saved
	 */
	protected void executeCommandAndCheckSave(Command command, String title, String saveButtonText, String withoutSaveButtonText, String content, boolean alwaysShow) {
		if (!mEditor.isSaved() || alwaysShow) {
			Button saveThenExecuteButton = new TextButton(saveButtonText, mStyles.textButton.press);
			Button justExecuteButton = new TextButton(withoutSaveButtonText, mStyles.textButton.press);

			Command saveAndExecute = new CEditorSave(mEditor, command);

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
	 * Calculate top and bottom margin
	 * @pre initStyles() have to be called before using this method
	 * @return number of pixels to pad above and below the bars
	 */
	protected float getTopBottomPadding() {
		return mStyles.vars.barUpperLowerHeight + mStyles.vars.paddingOuter;
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
		TextButtons textButton = new TextButtons();
		Sliders slider = new Sliders();
		TextFields textField = new TextFields();
		Skins skin = new Skins();
		Labels label = new Labels();
		CheckBoxes checkBox = new CheckBoxes();
		ScrollPane scrollPane = new ScrollPane();
		Variables vars = new Variables();
		Colors colors = new Colors();

		static class Variables {
			float paddingDefault = 0;
			float paddingSeparator = 0;
			float paddingAfterLabel = 0;
			float paddingOuter = 0;
			float paddingInner = 0;
			float barUpperLowerHeight = 0;
			float textFieldNumberWidth = 0;
		}

		static class Colors {
			Color widgetBackground = null;
		}

		static class TextButtons {
			TextButtonStyle press = null;
			TextButtonStyle toggle = null;
			TextButtonStyle selected = null;
		}

		static class Sliders {
			SliderStyle standard = null;
		}

		static class TextFields {
			TextFieldStyle standard = null;
		}

		static class Skins {
			Skin general = null;
			Skin editor = null;
		}

		static class Labels {
			LabelStyle standard = null;
			LabelStyle error = null;
			LabelStyle highlight = null;
			LabelStyle success = null;
		}

		static class CheckBoxes {
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
	/** UI Factory for creating UI elements */
	protected UiPanelFactory mUiFactory = new UiPanelFactory();

	/** UI elements that should be disabled during publish */
	protected ArrayList<Actor> mDisabledWhenPublished = new ArrayList<>();
	/** Enemy highlight button */
	private Button mEnemyHighlight = null;
	/** Grid button */
	private Button mGridRender = null;
	/** Grid above button */
	private Button mGridRenderAbove = null;
	/** Editor scene */
	protected Editor mEditor = null;
	/** Editor menu table (upper left) */
	private AlignTable mEditorMenu = new AlignTable();
	/** File menu table (upper right) */
	private AlignTable mFileMenu = new AlignTable();
	/** Tool table (left) */
	protected AlignTable mToolMenu = new AlignTable();
	/** If the main table has a valid layout, false means the collision boxes
	 * will be updated once the main table has a valid layout again */
	private boolean mLayoutWasValid = false;
	/** All UI-bodies for collision */
	private ArrayList<Body> mBodies = null;
	/** The top bar */
	private Background mTopBar = null;
}
