package com.spiddekauga.voider.menu;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.voider.ClientVersions;
import com.spiddekauga.voider.menu.CreditScene.CreditName;
import com.spiddekauga.voider.menu.CreditScene.CreditSection;
import com.spiddekauga.voider.resources.SkinNames;
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

		initVars();
		initScrollPane();
		initHeader();
		initCredits();
		initFooter();
	};

	/**
	 * Initialize variables
	 */
	private void initVars() {
		mPaddingHeader = SkinNames.getResource(SkinNames.GeneralVars.PADDING_CREDITS_HEADER);
		mPaddingSection = SkinNames.getResource(SkinNames.GeneralVars.PADDING_CREDITS_SECTION);
		mSectionStyle = SkinNames.getResource(SkinNames.General.LABEL_CREDIT_SECTION);
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
		scrollPane.setTouchable(Touchable.childrenOnly);

		mMainTable.row();
		mMainTable.add(scrollPane).setSize(width, height);


		// Make able to scroll past whole
		mCreditTable.row().setHeight(height);
		mCreditTable.add();
	}

	/**
	 * Initialize information
	 */
	private void initHeader() {
		mUiFactory.addHeader("Voider " + ClientVersions.getLatest().toString(), mCreditTable);
		mCreditTable.row().setHeight(mPaddingHeader);
		mUiFactory.addHeader("Credits", mCreditTable);
	}

	/**
	 * Initialize credits
	 */
	private void initCredits() {
		ArrayList<CreditSection> creditSections = mScene.getCredits();

		// Sections
		for (CreditSection creditSection : creditSections) {
			mCreditTable.row();
			Label label = new Label(creditSection.sectionName, mSectionStyle);
			mCreditTable.add(label).setPadTop(mPaddingSection);

			// Names
			for (CreditName creditName : creditSection.names) {
				mCreditTable.row();
				// TODO
			}
		}
	}

	/**
	 * Initialize footer
	 */
	private void initFooter() {
		// TODO

		// Can scroll past whole
		mCreditTable.row().setHeight(Gdx.graphics.getHeight());
		mCreditTable.add();
	}

	/**
	 * Set the credit scene
	 * @param scene credit scene
	 */
	void setScene(CreditScene scene) {
		mScene = scene;
	}

	private LabelStyle mSectionStyle = null;
	private float mPaddingHeader = 0;
	private float mPaddingSection = 0;
	private CreditScene mScene = null;
	private AlignTable mCreditTable = new AlignTable();
}
