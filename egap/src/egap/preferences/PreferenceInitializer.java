package egap.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import egap.EgapPlugin;
import egap.quickfix.assisted_inject.QuickFixAssistedInjectModel;
import egap.quickfix.binding_creation.QuickFixBindingCreation;
import egap.quickfix.binding_creation.QuickFixProviderMethodCreation;
import egap.quickfix.module_creation.QuickFixCreateGuiceModule;
import egap.quickfix.module_installation.QuickFixInstallModule;
import egap.quickfix.navigate.QuickfixNavigateTo;
import egap.quickfix.provider_conversion.QuickFixProviderConversion;

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
		store.setDefault(QuickFixAssistedInjectModel.ENABLED_STATE_ID, true);
		store.setDefault(QuickFixCreateGuiceModule.ENABLED_STATE_ID, true);
		store.setDefault(QuickFixInstallModule.ENABLED_STATE_ID, true);
		store.setDefault(QuickFixProviderConversion.ENABLED_STATE_ID, true);
		store.setDefault(QuickfixNavigateTo.ENABLED_STATE_ID, true);
		store.setDefault(QuickFixBindingCreation.ENABLED_STATE_ID, true);
		store.setDefault(QuickFixProviderMethodCreation.ENABLED_STATE_ID, true);
		store.setDefault(EgapPlugin.DEBUG_MODE_ID, false);

		store.setDefault(EgapPlugin.ID_TEST_PACKAGE_PREFIX, "test");
		store.setDefault(EgapPlugin.ID_SRC_FOLDER, "src");
		store.setDefault(EgapPlugin.ID_TEST_SRC_FOLDER, "src-test");
		store.setDefault(EgapPlugin.ID_TEST_SUFFIX, "Test");
	}

}
