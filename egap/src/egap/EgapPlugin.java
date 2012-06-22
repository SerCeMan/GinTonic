package egap;

import java.util.List;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


import egap.guice.GuiceIndex;
import egap.guice.GuiceIndexSerializer;
import egap.quickfix.EgapQuickFix;
import egap.quickfix.assisted_inject.QuickFixAssistedInjectModel;
import egap.quickfix.binding_creation.QuickFixBindingCreation;
import egap.quickfix.binding_creation.QuickFixProviderMethodCreation;
import egap.quickfix.module_creation.QuickFixCreateGuiceModule;
import egap.quickfix.module_installation.QuickFixInstallModule;
import egap.quickfix.navigate.QuickfixNavigateTo;
import egap.quickfix.provider_conversion.QuickFixProviderConversion;
import egap.utils.ListUtils;

/**
 * The activator class controls the plug-in life cycle
 */
public class EgapPlugin extends AbstractUIPlugin implements IStartup {

	public static final String PLUGIN_ID = "com.jaculon.egap";
	public static final String DEBUG_MODE_ID = "DEBUG_MODE";

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
		Status status = new Status(severity, PLUGIN_ID, message, throwable);
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
		boolean isDebugModeEnabled = store.getBoolean(DEBUG_MODE_ID);
		return isDebugModeEnabled;
	}

}
