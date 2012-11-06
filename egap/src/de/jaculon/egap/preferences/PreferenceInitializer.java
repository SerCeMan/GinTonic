package de.jaculon.egap.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import de.jaculon.egap.EgapPlugin;


/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		EgapPlugin egapPlugin = EgapPlugin.getEgapPlugin();
		IPreferenceStore store = egapPlugin.getPreferenceStore();
		
		store.setDefault(EgapPlugin.ID_DEBUG_MODE, false);

		store.setDefault(EgapPlugin.ID_TEST_PACKAGE_PREFIX, "test");
		store.setDefault(EgapPlugin.ID_SRC_FOLDER, "src");
		store.setDefault(EgapPlugin.ID_TEST_SRC_FOLDER, "src-test");
		store.setDefault(EgapPlugin.ID_TEST_SUFFIX, "Test");
	}

}
