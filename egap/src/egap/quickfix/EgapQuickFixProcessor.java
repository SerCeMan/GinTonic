package egap.quickfix;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;
import org.eclipse.jface.preference.IPreferenceStore;

import com.google.common.collect.Lists;

import egap.EgapPlugin;
import egap.nature.EgapNature;

public class EgapQuickFixProcessor implements IQuickFixProcessor {

	@Override
	public boolean hasCorrections(ICompilationUnit unit, int problemId) {
		return false;
	}

	@Override
	public IJavaCompletionProposal[] getCorrections(IInvocationContext context,
			IProblemLocation[] locations) throws CoreException {

		/**
		 * The quick fixes are only enabled if the project has the egap nature.
		 * 
		 * Have to check this here, as the configuration in the plugin.xml:
		 * 
		 * <pre>
		 * <with variable="projectNatures"> 
		 * <equals value="egap.EgapNature"/> 
		 * </with>
		 * </pre>
		 * 
		 * does not do it!
		 */
		ICompilationUnit compilationUnit = context.getCompilationUnit();
		IJavaElement parent = compilationUnit.getParent();
		IJavaProject javaProject = parent.getJavaProject();
		IProject project = javaProject.getProject();

		if (!project.hasNature(EgapNature.ID)) {
			return null;
		}

		/*
		 * Simple benchmark to ensure that we perform a quickfix not a slooow
		 * fix !
		 */
		long now = System.currentTimeMillis();

		EgapPlugin egapPlugin = EgapPlugin.getEgapPlugin();
		List<EgapQuickFix> egapQuickfixes = egapPlugin.getQuickfixes();

		List<IJavaCompletionProposal> proposals = Lists.newLinkedList();

		int nrOfEnabledQuickFixes = 0;
		IPreferenceStore store = egapPlugin.getPreferenceStore();
		for (EgapQuickFix quickFix : egapQuickfixes) {
			/* The quick fixes can be disabled in the preferences */
			boolean isEnabled = store.getBoolean(quickFix.getEnabledStateID());
			if (isEnabled) {
				nrOfEnabledQuickFixes++;
				/* Every quickfix can contribute a proposal. */
				try {
					quickFix.addProposals(context, proposals);
				} catch (Exception e) {
					EgapPlugin.log(IStatus.ERROR, e.getMessage(), e);
				}
			}
		}

		long then = System.currentTimeMillis();
		long diff = then - now;

		if (diff > 50) {
			String message = "Egap QuickFix took " + diff + " ms, "
					+ nrOfEnabledQuickFixes + " fixes enabled.";
			EgapPlugin.logWarning(message);
		}

		if (proposals.isEmpty()) {
			return null;
		}

		return proposals.toArray(new IJavaCompletionProposal[proposals.size()]);
	}

}
