package ru.naumen.gintonic.plugin.icons;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;

import ru.naumen.gintonic.GinTonicIDs;
import ru.naumen.gintonic.GinTonicPlugin;

public class Icons {

    public static Image ginTonicDefaultIconSmall = getIconSmall();
    private static Image getIconSmall() {
        return safeOpenImage("icons/gintonic_icon_small.png");
    }
    

    public static Image ginTonicIconCreate = getIconCreate();
    private static Image getIconCreate() {
        return safeOpenImage("icons/gintonic_icon_create.png");
    }
    
    private static Image safeOpenImage(String path) {
        InputStream stream = null;
        try  {
            Bundle bundle = Platform.getBundle(GinTonicIDs.GINTONIC_PLUGIN_NAME); // plugin
            if(bundle == null) { // feature
                bundle = Platform.getBundle(GinTonicIDs.GINTONIC_FEATURE_NAME);
            }
            stream = bundle.getEntry(path).openStream();
            return new Image(Display.getDefault(), stream);
        } catch (Exception e) {
            GinTonicPlugin.logException("Can't get icon " + path, e);
            return null;
        } finally {
            if(stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    GinTonicPlugin.logException("Can't get icon " + path, e);
                    return null;
                }
            }
        }
    }
}
