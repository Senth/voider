package com.spiddekauga.voider.menu;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.voider.ClientVersions;
import com.spiddekauga.voider.menu.CreditScene.CreditName;
import com.spiddekauga.voider.menu.CreditScene.CreditSection;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.repo.resource.SkinNames.CreditImages;
import com.spiddekauga.voider.repo.resource.SkinNames.IImageNames;
import com.spiddekauga.voider.scene.Gui;

/**
 * UI for the credit scene
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class CreditGui extends Gui {

	@Override
	public void initGui() {
		super.initGui();

		setBackground(SkinNames.GeneralImages.BACKGROUND_SPACE, true);

		mCreditTable.setName("credit-table");

		initVars();
		initScrollPane();
		initHeader();
		initCredits();
		initFooter();
		initBackButton();
	};

	@Override
	public void update() {
		super.update();

		scrollCredits();
	}

	private void initBackButton() {
		ButtonListener buttonListener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mScene.back();
			}
		};

		addActor(mUiFactory.button.createBackButton(buttonListener));
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
	 * Initialize variables
	 */
	private void initVars() {
		mPaddingHeader = SkinNames.getResource(SkinNames.CreditsVars.PADDING_HEADER);
		mPaddingSection = SkinNames.getResource(SkinNames.CreditsVars.PADDING_SECTION);
		mPaddingLogo = SkinNames.getResource(SkinNames.CreditsVars.PADDING_LOGO);
		mSectionStyle = SkinNames.getResource(SkinNames.CreditsUi.LABEL_SECTION);
		mNameStyle = SkinNames.getResource(SkinNames.CreditsUi.LABEL_NAME);
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
		scrollPane.setTouchable(Touchable.disabled);
		scrollPane.setVelocityY(mScrollPaneSpeed);

		mMainTable.row();
		mMainTable.add(scrollPane).setSize(width, height);
	}

	/**
	 * Initialize information
	 */
	private void initHeader() {
		// Make able to scroll past whole
		mUiFactory.text.addHeader("Voider " + ClientVersions.getLatest().toString(), mCreditTable);
		mCreditTable.getRow().setPadTop(Gdx.graphics.getHeight());
		addHeader("Credits");
	}

	/**
	 * Initialize credits
	 */
	private void initCredits() {
		ArrayList<CreditSection> creditSections = mScene.getCredits();

		float padNames = mUiFactory.getStyles().vars.paddingInner / 2;

		// Sections
		for (CreditSection creditSection : creditSections) {
			mCreditTable.row();
			Label label = new Label(creditSection.sectionName, mSectionStyle);
			mCreditTable.add(label).setPadTop(mPaddingSection).setPadBottom(mPaddingSection / 2);

			// Names
			for (CreditName creditName : creditSection.names) {
				mCreditTable.row().setPadBottom(mUiFactory.getStyles().vars.paddingOuter).setAlign(Horizontal.CENTER);

				label = new Label(creditName.firstName, mNameStyle);
				label.setAlignment(Align.right);
				mCreditTable.add(label).setPadRight(padNames);

				label = new Label(creditName.lastName, mNameStyle);
				mCreditTable.add(label).setPadLeft(padNames);
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
	 * Add text header to the credit table (with correct padding)
	 * @param text the text to display
	 */
	private void addHeader(String text) {
		mUiFactory.text.addHeader(text, mCreditTable);
		mCreditTable.getRow().setPadTop(mPaddingHeader);
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


	/**
	 * Set the credit scene
	 * @param scene credit scene
	 */
	void setScene(CreditScene scene) {
		mScene = scene;
	}

	private float mScrollY = 0;
	private LabelStyle mNameStyle = null;
	private LabelStyle mSectionStyle = null;
	private float mPaddingHeader = 0;
	private float mPaddingSection = 0;
	private float mPaddingLogo = 0;
	private float mScrollPaneSpeed = 0;
	private float mScrollRestartTime = 0;
	private CreditScene mScene = null;
	private AlignTable mCreditTable = new AlignTable();
	private ScrollPane mScrollPane = null;
}
