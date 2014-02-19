package com.spiddekauga.voider.editor;

import java.util.ArrayList;

import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.repo.ResourceRepo;
import com.spiddekauga.voider.resources.Def;

/**
 * Campaign editor for combining existing levels into a campaign.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CampaignEditor extends Editor {
	/**
	 * Default constructor
	 */
	public CampaignEditor() {
		super(new CampaignEditorGui(), 0);

		((EditorGui)mGui).setEditor(this);
	}

	@Override
	public void newDef() {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveDef() {
		saveToFile();
	}

	@Override
	public void saveDef(Command command) {
		saveToFile();
		if (command != null) {
			command.execute();
		}
	}

	@Override
	public void loadDef() {
		// TODO Auto-generated method stub

	}

	@Override
	public void duplicateDef() {
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
	protected void onActivate(Outcomes outcome, Object message) {
		super.onActivate(outcome, message);

		// TODO
	}

	@Override
	public ArrayList<Def> getNonPublishedDependencies() {
		// TODO
		return ResourceRepo.getNonPublishedDependencies(null);
	}

	@Override
	public void publishDef() {
		// TODO Auto-generated method stub

	}
}
