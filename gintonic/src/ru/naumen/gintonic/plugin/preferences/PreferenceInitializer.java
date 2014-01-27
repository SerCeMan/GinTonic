package ru.naumen.gintonic.plugin.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import ru.naumen.gintonic.GinTonicIDs;
import ru.naumen.gintonic.GinTonicPlugin;


/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		GinTonicPlugin ginTonicPlugin = GinTonicPlugin.getGinTonicPlugin();
		IPreferenceStore store = ginTonicPlugin.getPreferenceStore();
		store.setDefault(GinTonicIDs.DEBUG_MODE, false);
	}

}
