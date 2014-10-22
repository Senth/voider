package com.spiddekauga.voider.editor;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.SliderListener;
import com.spiddekauga.utils.scene.ui.TooltipWidget.ITooltip;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.utils.Messages;

/**
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ShipEditorGui extends ActorGui {

	@Override
	public void initGui() {
		super.initGui();

		initMovement();
	}

	@Override
	public void dispose() {
		mWidgets.dispose();

		super.dispose();
	}

	@Override
	public void resetValues() {
		super.resetValues();

		resetMovement();
	}

	@Override
	protected void initSettingsMenu() {
		super.initSettingsMenu();

		// Visual
		ImageButtonStyle buttonStyle = SkinNames.getResource(SkinNames.EditorIcons.VISUALS);
		Button button = mSettingTabs.addTab(buttonStyle, getVisualTable(), getVisualHider());
		mTooltip.add(button, Messages.EditorTooltips.TAB_VISUAL);

		// Movement
		buttonStyle = SkinNames.getResource(SkinNames.EditorIcons.SHIP_SETTINGS);
		mSettingTabs.addTab(buttonStyle, mWidgets.movement.table);
	}

	/**
	 * Initialize movement settings
	 */
	private void initMovement() {
		AlignTable table = mWidgets.movement.table;


		// Movement
		mUiFactory.addPanelSection("Movement settings", table, null);

		// Force
		SliderListener sliderListener = new SliderListener(mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mShipEditor.setMaxForce(newValue);
			}
		};
		mWidgets.movement.force = mUiFactory.addSlider("Force", Config.Editor.Ship.FORCE_MIN, Config.Editor.Ship.FORCE_MAX,
				Config.Editor.Ship.FORCE_STEP_SIZE, sliderListener, table, null, null);

		// Frequency
		sliderListener = new SliderListener(mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mShipEditor.setFrequency(newValue);
			}
		};
		mWidgets.movement.frequency = mUiFactory.addSlider("Frequency", Config.Editor.Ship.FREQUENCY_MIN, Config.Editor.Ship.FREQUENCY_MAX,
				Config.Editor.Ship.FREQUENCY_STEP_SIZE, sliderListener, table, null, null);

		// Dampening
		sliderListener = new SliderListener(mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mShipEditor.setDampening(newValue);
			}
		};
		mWidgets.movement.dampening = mUiFactory.addSlider("Dampening", Config.Editor.Ship.DAMPENING_MIN, Config.Editor.Ship.DAMPENING_MAX,
				Config.Editor.Ship.DAMPENING_STEP_SIZE, sliderListener, table, null, null);


		// Body settings
		mUiFactory.addPanelSection("Body settings", table, null);

		// Density
		sliderListener = new SliderListener(mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mShipEditor.setDensity(newValue);
			}
		};
		mWidgets.movement.density = mUiFactory.addSlider("Density", Config.Editor.Ship.DENSITY_MIN, Config.Editor.Ship.DENSITY_MAX,
				Config.Editor.Ship.DENSITY_STEP_SIZE, sliderListener, table, null, null);

		// Friction
		sliderListener = new SliderListener(mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mShipEditor.setFriction(newValue);
			}
		};
		mWidgets.movement.friction = mUiFactory.addSlider("Friction", Config.Editor.Ship.FRICTION_MIN, Config.Editor.Ship.FRICTION_MAX,
				Config.Editor.Ship.FRICTION_STEP_SIZE, sliderListener, table, null, null);

		// Elasticity
		sliderListener = new SliderListener(mInvoker) {
			@Override
			protected void onChange(float newValue) {
				mShipEditor.setElasticity(newValue);
			}
		};
		mWidgets.movement.elasticity = mUiFactory.addSlider("Elasticity", Config.Editor.Ship.ELASTICITY_MIN, Config.Editor.Ship.ELASTICITY_MAX,
				Config.Editor.Ship.ELASTICITY_STEP_SIZE, sliderListener, table, null, null);

	}

	/**
	 * Reset movement settings
	 */
	private void resetMovement() {
		mWidgets.movement.dampening.setValue(mShipEditor.getDampening());
		mWidgets.movement.density.setValue(mShipEditor.getDensity());
		mWidgets.movement.force.setValue(mShipEditor.getMaxForce());
		mWidgets.movement.frequency.setValue(mShipEditor.getFrequency());
		mWidgets.movement.friction.setValue(mShipEditor.getFriction());
		mWidgets.movement.elasticity.setValue(mShipEditor.getElasticity());
	}

	/**
	 * Bind this GUI to the specified ship editor
	 * @param shipEditor the ship editor
	 */
	void setShipEditor(ShipEditor shipEditor) {
		mShipEditor = shipEditor;
		setActorEditor(shipEditor);
	}

	@Override
	ITooltip getFileNewTooltip() {
		return Messages.EditorTooltips.FILE_NEW_SHIP;
	}

	@Override
	ITooltip getFileDuplicateTooltip() {
		return Messages.EditorTooltips.FILE_DUPLICATE_SHIP;
	}

	@Override
	ITooltip getFilePublishTooltip() {
		return null;
	}

	@Override
	ITooltip getFileInfoTooltip() {
		return Messages.EditorTooltips.FILE_INFO_SHIP;
	}

	@Override
	protected String getResourceTypeName() {
		return "ship";
	}

	private ShipEditor mShipEditor = null;
	private InnerWidgets mWidgets = new InnerWidgets();

	/**
	 * All inner widgets
	 */
	private static class InnerWidgets implements Disposable {
		MovementWidgets movement = new MovementWidgets();

		static class MovementWidgets implements Disposable {
			AlignTable table = new AlignTable();
			Slider frequency = null;
			Slider force = null;
			Slider dampening = null;
			Slider density = null;
			Slider friction = null;
			Slider elasticity = null;

			@Override
			public void dispose() {
				table.dispose();
			}
		}

		@Override
		public void dispose() {
			movement.dispose();
		}
	}
}
