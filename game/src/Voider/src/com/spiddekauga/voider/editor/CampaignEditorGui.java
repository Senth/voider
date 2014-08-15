package com.spiddekauga.voider.editor;

import com.spiddekauga.utils.scene.ui.TooltipWidget.ITooltip;
import com.spiddekauga.voider.utils.Messages;

/**
 * Campaign editor GUI
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CampaignEditorGui extends EditorGui {

	@Override
	public void initGui() {
		super.initGui();

		// TODO init gui
	}

	@Override
	protected void showInfoDialog() {
		// TODO Auto-generated method stub
	}

	@Override
	protected String getResourceTypeName() {
		return "campaign";
	}

	@Override
	public void setInfoNameError(String errorText) {
		// TODO Auto-generated method stub

	}

	@Override
	ITooltip getFileNewTooltip() {
		return Messages.EditorTooltips.FILE_NEW_CAMPAIGN;
	}

	@Override
	ITooltip getFileDuplicateTooltip() {
		return null;
	}

	@Override
	ITooltip getFilePublishTooltip() {
		return Messages.EditorTooltips.FILE_PUBLISH_CAMPAIGN;
	}

	@Override
	ITooltip getFileInfoTooltip() {
		return Messages.EditorTooltips.FILE_INFO_CAMPAIGN;
	}

}
