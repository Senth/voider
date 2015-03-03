#ifdef GL_ES
precision highp float;
#endif
varying vec4 v_col;
void main() {
   gl_FragColor = v_col;
}