package openGL.painter;

import openGL.drawable.Drawable;
import openGL.drawable.DrawableFunction;
import openGL.drawable.DrawableFunction111;

public abstract class PainterFactory {
	public static Painter createPainter(Drawable drawable) {
		if(drawable instanceof DrawableFunction) {
			if(drawable instanceof DrawableFunction111) {

			}
		}else {

		}
		return null;
	}
}
