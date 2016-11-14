package com.spiddekauga.voider.menu;

import com.spiddekauga.voider.editor.BulletEditor;
import com.spiddekauga.voider.editor.EnemyEditor;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.editor.ShipEditor;
import com.spiddekauga.voider.scene.SceneSwitcher;

/**
 * Scene for selecting editor
 */
class EditorSelectScene extends MenuScene {
/**
 * Default constructor
 */
public EditorSelectScene() {
	super(new EditorSelectGui());
	getGui().setScene(this);
}

@Override
protected EditorSelectGui getGui() {
	return (EditorSelectGui) super.getGui();
}

/**
 * Go to level editor
 */
void gotoLevelEditor() {
	SceneSwitcher.switchTo(new LevelEditor());
}

/**
 * Go to enemy editor
 */
void gotoEnemyEditor() {
	SceneSwitcher.switchTo(new EnemyEditor());
}

/**
 * Go to bullet editor
 */
void gotoBulletEditor() {
	SceneSwitcher.switchTo(new BulletEditor());
}

/**
 * Go to ship editor
 */
void gotoShipEditor() {
	SceneSwitcher.switchTo(new ShipEditor());
}
}
