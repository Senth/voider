package com.spiddekauga.voider.editor.brushes;

import java.util.UUID;

import com.badlogic.gdx.graphics.Color;
import com.spiddekauga.utils.ShapeRendererEx;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.voider.Config.Graphics.RenderOrders;
import com.spiddekauga.voider.resources.IResourceEditorRender;
import com.spiddekauga.voider.resources.Resource;

/**
 * Common class for all editor brushes.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class Brush extends Resource implements IResourceEditorRender {

	/**
	 * Creates a brush with a color
	 * @param color brush color
	 */
	protected Brush(Color color) {
		mUniqueId = UUID.randomUUID();
		mColor.set(color);
	}

	/**
	 * Sets the shape type. Default: Line
	 * @param shapeType
	 */
	protected void setShapeType(ShapeType shapeType) {
		mShapeType = shapeType;
	}

	/**
	 * Renders the brush
	 * @param shapeRenderer
	 */
	protected abstract void render(ShapeRendererEx shapeRenderer);

	/**
	 * Called before {@link #render(ShapeRendererEx)}. Render is only called if this
	 * method returns true.
	 * @return true if {@link #render(ShapeRendererEx)} should be called
	 */
	protected boolean preRender() {
		return true;
	}

	@Override
	public void renderEditor(ShapeRendererEx shapeRenderer) {
		RenderOrders.offsetZValueEditor(shapeRenderer, this);

		shapeRenderer.push(mShapeType);
		shapeRenderer.setColor(mColor);
		render(shapeRenderer);
		shapeRenderer.pop();

		RenderOrders.resetZValueOffsetEditor(shapeRenderer, this);
	}

	@Override
	public RenderOrders getRenderOrder() {
		return RenderOrders.BRUSH;
	}

	private Color mColor = new Color();
	private ShapeType mShapeType = ShapeType.Line;
}
