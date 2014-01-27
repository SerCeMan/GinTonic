package ru.naumen.gintonic.icons;

import java.io.InputStream;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import ru.naumen.gintonic.GinTonicPlugin;

public class Icons {

    public static Image ginTonicDefaultIconSmall = getIconSmall();
    private static Image getIconSmall() {
        try (InputStream stream = Platform.getBundle("gintonic").getEntry("icons/gintonic_icon_small.png").openStream()) {
            return new Image(Display.getDefault(), stream);
        } catch (Exception e) {
            GinTonicPlugin.logException("Can't get small icon", e);
            return null;
        }
    }

    public static Image ginTonicIconCreate = getIconCreate();
    private static Image getIconCreate() {
        try (InputStream stream = Platform.getBundle("gintonic").getEntry("icons/gintonic_icon_create.png").openStream()) {
            return new Image(Display.getDefault(), stream);
        } catch (Exception e) {
            GinTonicPlugin.logException("Can't get create icon", e);
            return null;
        }
    }
}
