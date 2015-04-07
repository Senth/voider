package com.spiddekauga.voider.editor;

import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.utils.event.GameEvent;

/**
 * Campaign editor for combining existing levels into a campaign.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CampaignEditor extends Editor {
	/**
	 * Default constructor
	 */
	public CampaignEditor() {
		super(new CampaignEditorGui(), 0, null);

		getGui().setEditor(this);
	}

	@Override
	public void newDef() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void saveImpl(Command command) {
		saveToFile();
		if (command != null) {
			command.execute();
		}
	}

	@Override
	public void handleEvent(GameEvent event) {
		// TODO Auto-generated method stub
	}

	@Override
	public void duplicateDef(String name, String description) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isDrawing() {
		return false;
	}

	@Override
	protected void saveToFile() {
		// TODO Auto-generated method stub

		setSaved();
	}

	@Override
	protected void onActivate(Outcomes outcome, Object message, Outcomes loadingOutcome) {
		super.onActivate(outcome, message, loadingOutcome);

		// TODO Auto-generated method stub
	}

	@Override
	public void publishDef() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isPublished() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDescription(String description) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void undoJustCreated() {
		// TODO Auto-generated method stub

	}

	@Override
	public Def getDef() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected CampaignEditorGui getGui() {
		return (CampaignEditorGui) super.getGui();
	}
}
