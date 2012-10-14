package egap.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.framework.Version;

import egap.EgapPlugin;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class EgapPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public EgapPreferencePage() {
		super(GRID);
		EgapPlugin egapPlugin = EgapPlugin.getEgapPlugin();
		setPreferenceStore(egapPlugin.getPreferenceStore());
		Version version = EgapPlugin.getEgapPlugin().getBundle().getVersion();
		setDescription("Version '" + version.toString() + "'");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {

		Composite fieldEditorParent = getFieldEditorParent();

		BooleanFieldEditor debugModeField = new BooleanFieldEditor(
				EgapPlugin.ID_DEBUG_MODE,
				"Debug mode",
				fieldEditorParent);
		addField(debugModeField);
		
		
		
		/* Preferences for the junit testcase creation command */
		
		addField(new StringFieldEditor(
				EgapPlugin.ID_TEST_SUFFIX,
				"Testcase suffix",
				fieldEditorParent));
		addField(new StringFieldEditor(
				EgapPlugin.ID_TEST_PACKAGE_PREFIX,
				"Test package prefix",
				fieldEditorParent));
		addField(new StringFieldEditor(
				EgapPlugin.ID_TEST_SRC_FOLDER,
				"Test src folder",
				fieldEditorParent));
		addField(new StringFieldEditor(
				EgapPlugin.ID_SRC_FOLDER,
				"Src folder",
				fieldEditorParent));
		
		
	}

	@Override
	public boolean performOk() {
		return super.performOk();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
	}

}