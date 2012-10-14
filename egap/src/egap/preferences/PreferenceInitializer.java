package egap.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import egap.EgapPlugin;

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
		
		store.setDefault(EgapPlugin.ID_QUICKFIXCREATEASSISTEDFACTORY_ENABLED_STATE, true);
		store.setDefault(EgapPlugin.ID_QUICKFIXCREATEGUICEMODULE_ENABLED_STATE_ID, true);
		store.setDefault(EgapPlugin.ID_QUICKFIXINSTALLMODULE_ENABLED_STATE, true);
		store.setDefault(EgapPlugin.ID_QUICKFIXPROVIDERCONVERSION_ENABLED_STATE, true);
		store.setDefault(EgapPlugin.ID_QUICKFIXNAVIGATETO_ENABLED_STATE, true);
		store.setDefault(EgapPlugin.ID_QUICKFIXBINDINGCREATION_ENABLED_STATE, true);
		store.setDefault(EgapPlugin.ID_QUICKFIXPROVIDERMETHODCREATION_ENABLED_STATE, true);
		
		store.setDefault(EgapPlugin.ID_DEBUG_MODE, false);

		store.setDefault(EgapPlugin.ID_TEST_PACKAGE_PREFIX, "test");
		store.setDefault(EgapPlugin.ID_SRC_FOLDER, "src");
		store.setDefault(EgapPlugin.ID_TEST_SRC_FOLDER, "src-test");
		store.setDefault(EgapPlugin.ID_TEST_SUFFIX, "Test");
	}

}
