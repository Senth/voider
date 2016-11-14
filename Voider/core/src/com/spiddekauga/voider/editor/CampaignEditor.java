package com.spiddekauga.voider.editor;

import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.utils.event.GameEvent;

/**
 * Campaign editor for combining existing levels into a campaign.
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
protected CampaignEditorGui getGui() {
	return (CampaignEditorGui) super.getGui();
}

@Override
public void newDef() {
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
public void publishDef() {
	// TODO Auto-generated method stub

}

@Override
public boolean isPublished() {
	// TODO Auto-generated method stub
	return false;
}

@Override
public void undoJustCreated() {
	// TODO Auto-generated method stub

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
public String getDescription() {
	// TODO Auto-generated method stub
	return null;
}

@Override
public void setDescription(String description) {
	// TODO Auto-generated method stub

}

@Override
public Def getDef() {
	// TODO Auto-generated method stub
	return null;
}

@Override
protected void saveImpl(Command command) {
	saveToFile();
	if (command != null) {
		command.execute();
	}
}

@Override
protected void saveToFile() {
	// TODO Auto-generated method stub

	setSaved();
}

@Override
public void handleEvent(GameEvent event) {
	// TODO Auto-generated method stub
}

@Override
protected void onActivate(Outcomes outcome, Object message, Outcomes loadingOutcome) {
	super.onActivate(outcome, message, loadingOutcome);

	// TODO Auto-generated method stub
}

@Override
public void onResourceAdded(IResource resource, boolean isNew) {
	// TODO Auto-generated method stub

}

@Override
public void onResourceRemoved(IResource resource) {
	// TODO Auto-generated method stub

}
}
