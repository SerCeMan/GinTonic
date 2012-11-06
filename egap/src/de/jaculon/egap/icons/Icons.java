package de.jaculon.egap.icons;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class Icons {

	public static Image egapDefaultIconSmall = new Image(
			Display.getDefault(),
			Icons.class.getResourceAsStream("egap_icon_small.png"));

	public static Image egapIconGoto = new Image(
			Display.getDefault(),
			Icons.class.getResourceAsStream("egap_icon_goto.png"));

	public static Image egapIconCreate = new Image(
			Display.getDefault(),
			Icons.class.getResourceAsStream("egap_icon_create.png"));
}
