package com.spiddekauga.voider.editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.utils.Pools;

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
		if (null != null) {
			@SuppressWarnings("unchecked")
			HashSet<UUID> uuidDeps = Pools.hashSet.obtain();
			@SuppressWarnings("unchecked")
			ArrayList<Def> dependencies = Pools.arrayList.obtain();

			getNonPublishedDependencies(null, uuidDeps, dependencies);

			Pools.hashSet.free(uuidDeps);
			return dependencies;
		}
		return null;
	}

	@Override
	public void publishDef() {
		// TODO Auto-generated method stub

	}
}
