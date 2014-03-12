package com.spiddekauga.voider.game;

import com.spiddekauga.utils.ShapeRendererEx;
import com.spiddekauga.voider.Config.Graphics.RenderOrders;
import com.spiddekauga.voider.game.triggers.Trigger;
import com.spiddekauga.voider.game.triggers.TriggerAction.Reasons;

/**
 * Test trigger that essentially does nothing
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class TriggerStub extends Trigger {
	@Override
	public boolean isTriggered() {
		return true;
	}

	@Override
	protected Reasons getReason() {
		return null;
	}

	@Override
	protected Object getCauseObject() {
		return null;
	}

	@Override
	public void renderEditor(ShapeRendererEx shapeRenderer) {
	}

	@Override
	public RenderOrders getRenderOrder() {
		return RenderOrders.TRIGGER_ACTOR_ACTIVATE;
	}
}
