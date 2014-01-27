package ru.naumen.gintonic;

import java.util.List;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ru.naumen.gintonic.guice.GuiceIndex;
import ru.naumen.gintonic.guice.GuiceIndexSerializer;
import ru.naumen.gintonic.quickfix.EgapQuickFix;
import ru.naumen.gintonic.quickfix.assisted_inject.QuickFixAssistedInject;
import ru.naumen.gintonic.quickfix.binding_creation.QuickFixBindingCreation;
import ru.naumen.gintonic.quickfix.binding_creation.QuickFixProviderMethodCreation;
import ru.naumen.gintonic.quickfix.module_creation.QuickFixCreateGuiceModule;
import ru.naumen.gintonic.quickfix.module_installation.QuickFixInstallModule;
import ru.naumen.gintonic.quickfix.provider_conversion.QuickFixProviderConversion;
import ru.naumen.gintonic.utils.ListUtils;

/**
 * The activator class controls the plug-in life cycle
 */
public class EgapPlugin extends AbstractUIPlugin implements IStartup {

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

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		quickfixes.add(new QuickFixCreateGuiceModule());
		quickfixes.add(new QuickFixInstallModule());
		quickfixes.add(new QuickFixAssistedInject());
		quickfixes.add(new QuickFixProviderConversion());
		quickfixes.add(new QuickFixBindingCreation());
		quickfixes.add(new QuickFixProviderMethodCreation());
	}

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
		Status status = new Status(severity, EgapIDs.PLUGIN, message, throwable);
		logger.log(status);
	}

	@Override
	public void earlyStartup() {
		/*
		 * We have to enable the earlyStartup feature as the
		 * EgapToggleNatureAction's label must be set to one of Add/Remove.
		 */
		GuiceIndex guiceIndex = GuiceIndexSerializer.read();

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
		boolean isDebugModeEnabled = store.getBoolean(EgapIDs.DEBUG_MODE);
		return isDebugModeEnabled;
	}
}
