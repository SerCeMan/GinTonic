package ru.naumen.gintonic.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import ru.naumen.gintonic.EgapIDs;
import ru.naumen.gintonic.EgapPlugin;


/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		EgapPlugin egapPlugin = EgapPlugin.getEgapPlugin();
		IPreferenceStore store = egapPlugin.getPreferenceStore();
		store.setDefault(EgapIDs.DEBUG_MODE, false);
	}

}
