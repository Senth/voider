package com.badlogic.gdx.graphics.glutils;

/**
 * A more advanced shape renderer which allows custom shaders and lines
 * with multicolors.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ShapeRendererEx extends ShapeRenderer {
	/**
	 * Sets the shader for the shape renderer
	 * @param shaderProgram shader to be used for rendering shapes
	 */
	public void setShader(ShaderProgram shaderProgram) {
		if (renderer instanceof ImmediateModeRenderer20) {
			((ImmediateModeRenderer20)renderer).setShader(shaderProgram);
		}
	}
}
