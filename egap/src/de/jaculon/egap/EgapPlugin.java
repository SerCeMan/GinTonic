package de.jaculon.egap;

import java.util.List;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.jaculon.egap.guice.GuiceIndex;
import de.jaculon.egap.guice.GuiceIndexSerializer;
import de.jaculon.egap.quickfix.EgapQuickFix;
import de.jaculon.egap.quickfix.assisted_inject.QuickFixAssistedInjectModel;
import de.jaculon.egap.quickfix.binding_creation.QuickFixBindingCreation;
import de.jaculon.egap.quickfix.binding_creation.QuickFixProviderMethodCreation;
import de.jaculon.egap.quickfix.module_creation.QuickFixCreateGuiceModule;
import de.jaculon.egap.quickfix.module_installation.QuickFixInstallModule;
import de.jaculon.egap.quickfix.navigate.QuickfixNavigateTo;
import de.jaculon.egap.quickfix.provider_conversion.QuickFixProviderConversion;
import de.jaculon.egap.utils.ListUtils;



/**
 * The activator class controls the plug-in life cycle
 */
public class EgapPlugin extends AbstractUIPlugin implements IStartup {

	public static final String ID_PLUGIN = "de.jaculon.egap";

	/**
	 * Nicht ändern!
	 */
	public static final String ID_NATURE = "de.jaculon.egap.EgapNature";
	
	public static final String ID_DEBUG_MODE = "de.jaculon.egap.DEBUG_MODE";
	public static final String ID_TEST_SRC_FOLDER = "de.jaculon.egap.test_src_folder";
	public static final String ID_TEST_SUFFIX = "de.jaculon.egap.test_suffix";
	public static final String ID_TEST_PACKAGE_PREFIX = "de.jaculon.egap.package_prefix";
	public static final String ID_SRC_FOLDER = "de.jaculon.egap.src_folder";

	private static EgapPlugin egapPlugin;

	private List<EgapQuickFix> quickfixes = ListUtils.newArrayList();

	public EgapPlugin() {
		egapPlugin = this;
	}

	public static EgapPlugin getEgapPlugin() {
		return egapPlugin;
	}

	public List<EgapQuickFix> getQuickfixes() {
		return quickfixes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		quickfixes.add(new QuickFixCreateGuiceModule());
		quickfixes.add(new QuickFixInstallModule());
		quickfixes.add(new QuickFixAssistedInjectModel());
		quickfixes.add(new QuickFixProviderConversion());
		quickfixes.add(new QuickfixNavigateTo());
		quickfixes.add(new QuickFixBindingCreation());
		quickfixes.add(new QuickFixProviderMethodCreation());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		GuiceIndexSerializer.write();
		super.stop(context);
	}

	public static void log(int severity, String message) {
		log(severity, message, null);
	}

	public static void logInfo(String message) {
		log(IStatus.INFO, message);
	}

	public static void logWarning(String message) {
		log(IStatus.WARNING, message);
	}

	public static void logException(Throwable throwable) {
		log(IStatus.ERROR, throwable.getMessage(), throwable);
	}

	public static void logException(String message, Throwable throwable) {
		log(IStatus.ERROR, message + "." + throwable.getMessage(), throwable);
	}

	public static void log(int severity, String message, Throwable throwable) {
		if (severity == IStatus.ERROR || severity == IStatus.WARNING) {
			doLog(severity, message, throwable);
		}
		else {
			if (isDebugMode()) {
				doLog(severity, message, throwable);
			}
		}
	}

	public static void doLog(int severity, String message, Throwable throwable) {
		EgapPlugin egapPlugin = getEgapPlugin();
		ILog logger = egapPlugin.getLog();
		Status status = new Status(severity, ID_PLUGIN, message, throwable);
		logger.log(status);
	}

	@Override
	public void earlyStartup() {
		/*
		 * We have to enable the earlyStartup feature as the
		 * EgapToggleNatureAction's label must be set to one of Add/Remove.
		 */
		GuiceIndex guiceIndex = null;
		try {
			guiceIndex = GuiceIndexSerializer.read();
		} catch (Exception e) {
			EgapPlugin.logException("Error deserializing Guice index!", e);
		}

		if (guiceIndex != null) {
			GuiceIndex.set(guiceIndex);
			guiceIndex.setFromDisc(true);
		}
		else {
			/*
			 * Create a new Guice index.
			 */
			GuiceIndex.get();
		}
	}

	public static boolean isDebugMode() {
		IPreferenceStore store = egapPlugin.getPreferenceStore();
		boolean isDebugModeEnabled = store.getBoolean(ID_DEBUG_MODE);
		return isDebugModeEnabled;
	}


}
