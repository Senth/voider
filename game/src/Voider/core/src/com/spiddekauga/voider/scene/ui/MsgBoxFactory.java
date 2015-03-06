package com.spiddekauga.voider.scene.ui;

import java.util.Stack;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.spiddekauga.utils.Strings;
import com.spiddekauga.utils.commands.CBugReportSend;
import com.spiddekauga.utils.commands.CGameQuit;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.MsgBox;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.utils.scene.ui.validate.VFieldLength;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Menu.IC_Time;
import com.spiddekauga.voider.network.misc.BugReportEntity.BugReportTypes;
import com.spiddekauga.voider.repo.analytics.AnalyticsRepo;
import com.spiddekauga.voider.repo.misc.SettingRepo;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.scene.ui.ButtonFactory.TabRadioWrapper;
import com.spiddekauga.voider.scene.ui.UiStyles.CheckBoxStyles;
import com.spiddekauga.voider.scene.ui.UiStyles.LabelStyles;
import com.spiddekauga.voider.scene.ui.UiStyles.TextButtonStyles;
import com.spiddekauga.voider.utils.Messages;
import com.spiddekauga.voider.utils.User;
import com.spiddekauga.voider.utils.commands.CSyncFixConflict;
import com.spiddekauga.voider.utils.commands.CUserConnect;
import com.spiddekauga.voider.utils.commands.CUserSetAskGoOnline;

/**
 * Message box factory
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class MsgBoxFactory {
	/**
	 * Package constructor
	 */
	MsgBoxFactory() {
		// Does nothing
	}

	/**
	 * Init the message box factory
	 * @param styles UI styles
	 */
	void init(UiStyles styles) {
		mStyles = styles;
		mUiFactory = UiFactory.getInstance();

		// Set msgbox default fade in time
		IC_Time time = ConfigIni.getInstance().menu.time;
		MsgBox.setFadeInTime(time.getMsgBoxFadeIn());
		MsgBox.setFadeOutTime(time.getMsgBoxFadeOut());
	}

	/**
	 * Show conflict window
	 */
	public synchronized void conflictWindow() {
		MsgBoxExecuter msgBox = add("Sync Conflict");

		// Set layout
		float buttonWidth = mUiFactory.getStyles().vars.textButtonWidth * 1.5f;
		msgBox.contentRow();
		msgBox.content().width(buttonWidth);
		msgBox.content().width(buttonWidth);
		msgBox.content().width(buttonWidth);
		msgBox.contentRow();
		msgBox.getContentTable().padBottom(0);
		msgBox.getButtonTable().padTop(0);

		// Text
		// @formatter:off
		String text = "Due to some event, one or more of your levels, enemies, or bullets are in conflict.\n"
				+ "Which version would you like to keep?\n\n"
				+ "Choose carefully! As the other choice will be discarded.";
		Label label = mUiFactory.text.create(text, true, LabelStyles.ERROR);
		label.setAlignment(Align.center);
		label.setWidth(buttonWidth * 3);
		msgBox.content(label).width(buttonWidth * 3).colspan(3);
		// @formatter:on


		// Button text
		msgBox.contentRow();
		msgBox.content("Cloud Version", Align.center).padTop(mUiFactory.getStyles().vars.paddingSeparator);
		msgBox.content();
		msgBox.content("Local Version", Align.center).padTop(mUiFactory.getStyles().vars.paddingSeparator);

		// Button icon
		msgBox.contentRow();
		Image image = new Image(SkinNames.getDrawable(SkinNames.GeneralImages.SYNC_CLOUD));
		msgBox.content(image);
		msgBox.content();
		image = new Image(SkinNames.getDrawable(SkinNames.GeneralImages.SYNC_DEVICE));
		msgBox.content(image);

		// Buttons
		msgBox.button("Download & Continue", new CSyncFixConflict(false));
		msgBox.getButtonCell().setPadRight(buttonWidth).setWidth(buttonWidth);
		msgBox.button("Upload & Continue", new CSyncFixConflict(true));
		msgBox.getButtonCell().setWidth(buttonWidth);
	}

	/**
	 * Show a custom bug report window
	 */
	public void bugReport() {
		MsgBoxExecuter msgBox = add("Bug Report / Feature Request");

		AlignTable content = new AlignTable();
		float width = mStyles.vars.textFieldWidth * 2 + mStyles.vars.paddingInner;
		content.setWidth(width);
		content.setKeepWidth(true);

		AlignTable left = new AlignTable();
		AlignTable right = new AlignTable();
		left.setAlign(Horizontal.LEFT, Vertical.MIDDLE);
		right.setAlign(Horizontal.LEFT, Vertical.MIDDLE);


		// Info text
		Label infoLabel = mUiFactory.text.create("Report a bug or send a feature request. All bugs and features requests are read :)\n "
				+ "Thank you for reporting a bug or sending a feature request :)\n //Matteus");
		infoLabel.setAlignment(Align.left);
		infoLabel.setWrap(true);
		content.row().setFillWidth(true);
		content.add(infoLabel).setWidth(width).setPadBottom(mUiFactory.getStyles().vars.paddingInner);

		Gui gui = SceneSwitcher.getGui();
		final CBugReportSend bugReportSend = new CBugReportSend(gui);

		// Bug / Feature
		TabRadioWrapper bugTab = mUiFactory.button.createTabRadioWrapper("Bug Report");
		TabRadioWrapper featureTab = mUiFactory.button.createTabRadioWrapper("Feature Request");

		bugTab.setListener(new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				bugReportSend.setType(BugReportTypes.BUG_CUSTOM);
			}
		});
		featureTab.setListener(new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				bugReportSend.setType(BugReportTypes.FEATURE);
			}
		});

		mUiFactory.button.addTabs(content, null, false, null, null, bugTab, featureTab);

		// Subject
		TextFieldListener subjectListener = new TextFieldListener() {
			@Override
			protected void onDone(String newText) {
				bugReportSend.setSubject(newText);
			}
		};
		TextField subject = mUiFactory.addTextField("Subject", true, "[EX: Can't place enemy]", width, subjectListener, content, null);
		Label subjectError = mUiFactory.text.getLastCreatedErrorLabel();
		subject.setMaxLength(50);
		VFieldLength validateSubjectLength = new VFieldLength(subjectListener, subjectError, 6);

		// Description
		TextFieldListener descriptionListener = new TextFieldListener() {
			@Override
			protected void onDone(String newText) {
				bugReportSend.setDescription(newText);
			}
		};
		mUiFactory.addTextArea("Detailed description", true,
				"[EX: Doesn't work to place any stationary enemy in the level editor. Tried with two...]", width, descriptionListener, content, null);
		Label descriptionError = mUiFactory.text.getLastCreatedErrorLabel();
		content.getCell().setHeight(content.getCell().getHeight() * 1.5f);
		VFieldLength validateDescriptionLength = new VFieldLength(descriptionListener, descriptionError, 30);

		msgBox.content(content);
		msgBox.button("Cancel");
		msgBox.button("Send", bugReportSend, validateDescriptionLength, validateSubjectLength);
	}

	/**
	 * Show a bug report window
	 * @param exception the exception that was thrown, null if no exception was thrown
	 */
	public void bugReport(final Exception exception) {
		MsgBoxExecuter msgBox = add("Bug Report");

		AlignTable content = new AlignTable();
		float width = mStyles.vars.textFieldWidth * 2 + mStyles.vars.paddingInner;
		content.setWidth(width);
		content.setKeepWidth(true);

		AlignTable left = new AlignTable();
		AlignTable right = new AlignTable();
		left.setAlign(Horizontal.LEFT, Vertical.MIDDLE);
		right.setAlign(Horizontal.LEFT, Vertical.MIDDLE);


		// Error text
		Label errorLabel = mUiFactory.text.create(Messages.Error.BUG_REPORT_INFO, LabelStyles.WARNING);
		errorLabel.setAlignment(Align.center);
		errorLabel.setWrap(true);
		content.row().setFillWidth(true);
		content.add(errorLabel).setWidth(width).setPadBottom(mUiFactory.getStyles().vars.paddingSeparator);
		content.row();

		Gui gui = SceneSwitcher.getGui();
		final CBugReportSend bugReportSend = new CBugReportSend(gui, exception);

		// Subject
		TextFieldListener subjectListener = new TextFieldListener() {
			@Override
			protected void onDone(String newText) {
				bugReportSend.setSubject(newText);
			}
		};
		TextField subject = mUiFactory.addTextField("Subject", false, "[EX: Can't place enemy]", subjectListener, content, null);
		subject.setMaxLength(50);
		content.getCell().setWidth(width);


		// Description
		TextFieldListener descriptionListener = new TextFieldListener() {
			@Override
			protected void onDone(String newText) {
				bugReportSend.setDescription(newText);
			}
		};
		mUiFactory.addTextArea("Detailed description (optional)", false,
				"[EX: Doesn't work to place any stationary enemy in the level editor. Tried with two...]", descriptionListener, content, null);
		content.getCell().setWidth(width);
		content.getCell().setHeight(content.getCell().getHeight() * 1.5f);


		CGameQuit quit = new CGameQuit();


		// Send anonymously
		ButtonListener buttonListener = new ButtonListener() {
			@Override
			protected void onChecked(Button button, boolean checked) {
				bugReportSend.setSendAnonymously(checked);
				SettingRepo.getInstance().network().setBugReportSendAnonymously(checked);
			}
		};
		Button button = mUiFactory.button.addCheckBoxRow("Send anonymously (I can't answer you or ask further questions)", CheckBoxStyles.CHECK_BOX,
				buttonListener, null, content);
		button.setChecked(SettingRepo.getInstance().network().isBugReportSentAnonymously());


		// Additional information that is sent
		buttonListener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				MsgBoxExecuter msgBox = add("Additional Error Information");

				AlignTable outerTable = new AlignTable();
				AlignTable table = new AlignTable();
				ScrollPane scrollPane = new ScrollPane(table);
				outerTable.setPaddingRowDefault(0, 0, mUiFactory.getStyles().vars.paddingInner, 0);


				mUiFactory.text
						.add("Additional information sent in the bug report\n(might appear to be unreadable)", outerTable, LabelStyles.WARNING);
				outerTable.row();
				outerTable.add(scrollPane).setSize(Gdx.graphics.getWidth() * 0.7f, Gdx.graphics.getHeight() * 0.6f);
				msgBox.content(outerTable);

				mUiFactory.text.add(Strings.exceptionToString(exception), table);
				table.getCell().setPadBottom(mUiFactory.getStyles().vars.paddingInner);
				table.row();
				mUiFactory.text.add(AnalyticsRepo.getInstance().getSessionDebug(), table);

				msgBox.addCancelButtonAndKeys("Back");
			}
		};

		content.row().setPadTop(mUiFactory.getStyles().vars.paddingInner);
		mUiFactory.button.addText("View additional information that is sent", TextButtonStyles.LINK, content, buttonListener, null, null);

		msgBox.content(content);
		msgBox.button("Quit Game", quit);
		msgBox.button("Send Report and Quit Game", bugReportSend);
	}

	/**
	 * Show go online window
	 */
	public void goOnline() {
		if (User.getGlobalUser().isAskToGoOnline()) {
			MsgBoxExecuter msgBox = add("Go Online?");

			Label label = mUiFactory.text.create("To use online features you need to connect to the server.");
			msgBox.content(label);

			msgBox.addCancelButtonAndKeys("Cancel");
			msgBox.button("Cancel and don't ask again this session", new CUserSetAskGoOnline(false));
			msgBox.getButtonCell().resetWidth().setPadRight(mUiFactory.getStyles().vars.paddingInner);
			msgBox.button("Connect", new CUserConnect());
		}
	}

	/**
	 * Create 'update message box' to show an update message dialog
	 * @param updateRequired true if an update is required, false if update is optional
	 * @param message message to display
	 * @param changeLog all new changes to display
	 */
	public void updateMessage(boolean updateRequired, final String message, final String changeLog) {
		String title = updateRequired ? "Update Required" : "Update Available";
		MsgBoxExecuter msgBox = add(title);

		final int width = (int) (Gdx.graphics.getWidth() * 0.7f);

		Label label = new Label(message, LabelStyles.HIGHLIGHT.getStyle());
		label.setWrap(true);
		label.setWidth(width);
		label.setAlignment(Align.center);
		msgBox.content(label);

		// Add change-log
		msgBox.button("ChangeLog");
		new ButtonListener((Button) msgBox.getButtonCell().getActor()) {
			@Override
			protected void onPressed(Button button) {
				changeLog("ChangeLog", "New changes since your version", changeLog);
			}
		};

		msgBox.addCancelButtonAndKeys("OK");
	}

	/**
	 * Create a change log message box
	 * @param title title of the message box
	 * @param topMessage additional message to display above the change log
	 * @param changeLog the change log
	 */
	public void changeLog(String title, String topMessage, String changeLog) {
		MsgBoxExecuter changeLogMsgBox = add(title);

		final int width = (int) (Gdx.graphics.getWidth() * 0.7f);
		final int maxHeight = Gdx.graphics.getHeight() / 2;

		Label label = mUiFactory.text.create(topMessage, true);
		label.setWidth(width);
		label.setAlignment(Align.center);

		changeLogMsgBox.content(label).padBottom(mStyles.vars.paddingSeparator);
		changeLogMsgBox.contentRow();


		label = mUiFactory.text.create(changeLog, false);

		// Too high, use scroll pane
		label.layout();
		if (label.getHeight() > maxHeight) {
			ScrollPane scrollPane = new ScrollPane(label, mStyles.scrollPane.noBackground);
			scrollPane.setFadeScrollBars(false);
			changeLogMsgBox.content(scrollPane).size(width, maxHeight);
		} else {
			changeLogMsgBox.content(label);
		}


		changeLogMsgBox.addCancelButtonAndKeys("OK");
	}

	/**
	 * Create a message box. This is automatically added to the current scene/gui
	 * @param title setting this automatically receives a style with a title. Set to null
	 *        to skip
	 * @return message box that was added to
	 */
	public synchronized MsgBoxExecuter add(String title) {
		Gui gui = SceneSwitcher.getGui();
		if (gui == null) {
			return null;
		}

		WindowStyle windowStyle = title != null ? mStyles.window.title : mStyles.window.noTitle;

		MsgBoxExecuter msgBox = null;
		if (!mFreeMsgBoxes.isEmpty()) {
			msgBox = mFreeMsgBoxes.pop();
		}

		if (msgBox == null) {
			msgBox = new MsgBoxExecuter(windowStyle);
			Skin skin = ResourceCacheFacade.get(InternalNames.UI_GENERAL);
			msgBox.setSkin(skin);
			msgBox.setButtonPad(mStyles.vars.paddingButton);
		} else {
			msgBox.setStyle(windowStyle);
		}


		msgBox.clear();

		if (title != null) {
			msgBox.setTitle(title);
		}
		gui.showMsgBox(msgBox);
		msgBox.invalidate();

		return msgBox;
	}

	/**
	 * Add a free message box (i.e. this is not longer used in the GUI instance)
	 * @param msgBox
	 */
	public synchronized void free(MsgBoxExecuter msgBox) {
		mFreeMsgBoxes.push(msgBox);
	}

	/** Inactive/free message boxes */
	private Stack<MsgBoxExecuter> mFreeMsgBoxes = new Stack<>();
	private UiStyles mStyles = null;
	private UiFactory mUiFactory = null;
}
