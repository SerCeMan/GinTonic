package ru.naumen.gintonic;

import java.util.List;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ru.naumen.gintonic.context.quickfix.GinTonicQuickFix;
import ru.naumen.gintonic.context.quickfix.assisted.QuickFixAssistedInject;
import ru.naumen.gintonic.context.quickfix.bindings.QuickFixBindingCreation;
import ru.naumen.gintonic.context.quickfix.bindings.QuickFixProviderMethodCreation;
import ru.naumen.gintonic.context.quickfix.modules.QuickFixCreateGuiceModule;
import ru.naumen.gintonic.context.quickfix.modules.QuickFixInstallModule;
import ru.naumen.gintonic.context.quickfix.moving.QuickFixGoToBinging;
import ru.naumen.gintonic.context.quickfix.moving.QuickFixGoToImpl;
import ru.naumen.gintonic.context.quickfix.providers.QuickFixProviderConversion;
import ru.naumen.gintonic.guice.GuiceIndex;
import ru.naumen.gintonic.guice.GuiceIndexSerializer;
import ru.naumen.gintonic.utils.ListUtils;

/**
 * The activator class controls the plug-in life cycle
 */
public class GinTonicPlugin extends AbstractUIPlugin implements IStartup {

	private static GinTonicPlugin ginTonicPlugin;

	private List<GinTonicQuickFix> quickfixes = ListUtils.newArrayList();


	public GinTonicPlugin() {
		ginTonicPlugin = this;
	}

	public static GinTonicPlugin getGinTonicPlugin() {
		return ginTonicPlugin;
	}

	public List<GinTonicQuickFix> getQuickfixes() {
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
		quickfixes.add(new QuickFixGoToBinging());
		quickfixes.add(new QuickFixGoToImpl());
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
		GinTonicPlugin ginTonicPlugin = getGinTonicPlugin();
		ILog logger = ginTonicPlugin.getLog();
		Status status = new Status(severity, GinTonicIDs.PLUGIN, message, throwable);
		logger.log(status);
	}

	@Override
	public void earlyStartup() {
		/*
		 * We have to enable the earlyStartup feature as the
		 * GinTonicToggleNatureAction's label must be set to one of Add/Remove.
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
		IPreferenceStore store = ginTonicPlugin.getPreferenceStore();
		boolean isDebugModeEnabled = store.getBoolean(GinTonicIDs.DEBUG_MODE);
		return isDebugModeEnabled;
	}
}
