package com.spiddekauga.voider.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.voider.menu.CreditScene.CreditLine;
import com.spiddekauga.voider.menu.CreditScene.CreditSection;
import com.spiddekauga.voider.settings.SettingRepo;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.repo.resource.SkinNames.CreditImages;
import com.spiddekauga.voider.repo.resource.SkinNames.IImageNames;
import com.spiddekauga.voider.scene.ui.UiStyles.LabelStyles;
import com.spiddekauga.voider.scene.ui.UiStyles.TextButtonStyles;

import java.util.ArrayList;

/**
 * UI for the credit scene
 */
class CreditGui extends MenuGui {

private float mScrollY = 0;

;
private LabelStyle mTextStyle = null;
private LabelStyle mSectionStyle = null;
private float mPaddingHeader = 0;
private float mPaddingSection = 0;
private float mPaddingLogo = 0;
private float mScrollPaneSpeed = 0;
private float mScrollRestartTime = 0;
private CreditScene mScene = null;
private AlignTable mCreditTable = new AlignTable();
private ScrollPane mScrollPane = null;

@Override
public void onCreate() {
	super.onCreate();

	mCreditTable.setName("credit-table");
	setDisposeAfterResize(true);

	initVars();
	initScrollPane();
	initHeader();
	initCredits();
	initFooter();

	addBackButton();
	addChangelogButton();
}

/**
 * Initialize variables
 */
private void initVars() {
	mPaddingHeader = SkinNames.getResource(SkinNames.CreditsVars.PADDING_HEADER);
	mPaddingSection = SkinNames.getResource(SkinNames.CreditsVars.PADDING_SECTION);
	mPaddingLogo = SkinNames.getResource(SkinNames.CreditsVars.PADDING_LOGO);
	mSectionStyle = LabelStyles.HEADER.getStyle();
	mTextStyle = SkinNames.getResource(SkinNames.CreditsUi.LABEL_NAME);
	mScrollPaneSpeed = SkinNames.getResource(SkinNames.CreditsVars.SCROLL_SPEED);
	mScrollRestartTime = SkinNames.getResource(SkinNames.CreditsVars.RESTART_TIME);
}

/**
 * Init ScrollPane that scrolls with the credits
 */
private void initScrollPane() {
	mMainTable.setAlign(Horizontal.CENTER, Vertical.MIDDLE);
	mCreditTable.setAlign(Horizontal.CENTER, Vertical.TOP);

	float width = Gdx.graphics.getWidth();
	float height = Gdx.graphics.getHeight();

	ScrollPane scrollPane = new ScrollPane(mCreditTable);
	mScrollPane = scrollPane;
	mScrollPane.setSmoothScrolling(false);
	scrollPane.setTouchable(Touchable.childrenOnly);
	// scrollPane.setScrollingDisabled(true, true);
	scrollPane.setVelocityY(mScrollPaneSpeed);

	mMainTable.row();
	mMainTable.add(scrollPane).setSize(width, height);
}

/**
 * Initialize information
 */
private void initHeader() {
	// Make able to scroll past whole
	mUiFactory.text.addHeader("Voider " + SettingRepo.getInstance().info().getCurrentVersion().getVersion(), mCreditTable);
	mCreditTable.getRow().setPadTop(Gdx.graphics.getHeight());
	addHeader("Credits");
}

/**
 * Initialize credits
 */
private void initCredits() {
	ArrayList<CreditSection> creditSections = mScene.getCredits();

	// Sections / Names
	for (CreditSection creditSection : creditSections) {
		// Image
		if (creditSection.hasImage()) {
			// TODO Credit image
		}

		// Section / Name
		mCreditTable.row();
		Label label = new Label(creditSection.sectionName, mSectionStyle);
		mCreditTable.add(label).setPadTop(mPaddingSection).setPadBottom(mPaddingSection / 2);


		// Text / Roles
		for (CreditLine creditLine : creditSection.texts) {
			addCreditLine(creditLine);
		}

		// URL
		if (creditSection.hasUrl()) {
			addCreditLine(creditSection.url);
		}

		// Twitter
		if (creditSection.hasTwitter()) {
			addCreditLine(creditSection.twitter);
		}
	}
}

/**
 * Initialize footer
 */
private void initFooter() {
	// Add all logos to be displayed
	for (CreditImages creditImage : CreditImages.values()) {
		addLogo(creditImage);
	}

	// Scroll past everything
	mCreditTable.getRow().setPadBottom(Gdx.graphics.getHeight());
	mCreditTable.layout();
	mScrollPane.layout();
}

/**
 * Add changelog button
 */
private void addChangelogButton() {
	Button button = mUiFactory.button.createImage(SkinNames.General.CHANGELOG_BIG);
	mUiFactory.button.addSound(button);
	new ButtonListener(button) {
		@Override
		protected void onPressed(Button button) {
			mUiFactory.msgBox.changeLog("ChangeLog", null, SettingRepo.getInstance().info().getVersions().getAll());
		}
	};

	AlignTable table = new AlignTable();
	table.setAlign(Horizontal.LEFT, Vertical.BOTTOM);
	table.add(button);
	addActor(table);
}

/**
 * Add text header to the credit table (with correct padding)
 * @param text the text to display
 */
private void addHeader(String text) {
	mUiFactory.text.addHeader(text, mCreditTable);
	mCreditTable.getRow().setPadTop(mPaddingHeader);
}

/**
 * Add a new line to the credit section
 * @param creditLine show credits
 */
private void addCreditLine(CreditLine creditLine) {
	mCreditTable.row().setPadBottom(mUiFactory.getStyles().vars.paddingOuter).setAlign(Horizontal.CENTER);

	// URL
	if (creditLine.hasLink()) {
		mUiFactory.button.addTextUrl(creditLine.getText(), creditLine.getUrl(), TextButtonStyles.LINK, mCreditTable, null, null);
	}
	// Regular Text
	else {
		mCreditTable.add(new Label(creditLine.getText(), mTextStyle));
	}
}

/**
 * Add a logo
 * @param logo an image to display
 */
private void addLogo(IImageNames logo) {
	Image image = new Image(SkinNames.getDrawable(logo));
	mCreditTable.row().setPadTop(mPaddingLogo);
	mCreditTable.add(image);
}

@Override
public void onDestroy() {
	super.onDestroy();

}

@Override
public void update() {
	super.update();

	scrollCredits();
}

/**
 * Update scroll pane position
 */
private void scrollCredits() {
	float deltaTime = Gdx.graphics.getDeltaTime();
	float scrollDist = deltaTime * mScrollPaneSpeed;
	float newScrollY = mScrollY + scrollDist;
	setScrollY(newScrollY);

	if (mScrollY >= mScrollPane.getVisualScrollY() + mScrollPaneSpeed * mScrollRestartTime) {
		setScrollY(0);
	}
}

/**
 * Set scroll Y
 * @param scrollY
 */
private void setScrollY(float scrollY) {
	mScrollY = scrollY;
	mScrollPane.setScrollY(scrollY);
}

/**
 * Set the credit scene
 * @param scene credit scene
 */
void setScene(CreditScene scene) {
	mScene = scene;
}
}
