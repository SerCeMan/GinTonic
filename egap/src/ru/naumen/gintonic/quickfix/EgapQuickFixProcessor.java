package ru.naumen.gintonic.quickfix;

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

import ru.naumen.gintonic.EgapPlugin;
import ru.naumen.gintonic.utils.IProjectUtils;
import ru.naumen.gintonic.utils.ListUtils;

public class EgapQuickFixProcessor implements IQuickFixProcessor {

    private boolean loggedWarningAboutProjectWithoutEgapNature;

    @Override
    public boolean hasCorrections(ICompilationUnit unit, int problemId) {
        return false;
    }

    @Override
    public IJavaCompletionProposal[] getCorrections(IInvocationContext context, IProblemLocation[] locations)
            throws CoreException {

        /**
         * The quick fixes are only enabled if the project has the
         * de.jaculon.egap nature.
         * 
         * Have to check this here, as the configuration in the plugin.xml:
         * 
         * <pre>
         * <with variable="projectNatures">
         * <equals value="de.jaculon.egap.EgapNature"/>
         * </with>
         * </pre>
         * 
         * does not do it!
         */
        ICompilationUnit compilationUnit = context.getCompilationUnit();
        IJavaElement parent = compilationUnit.getParent();
        IJavaProject javaProject = parent.getJavaProject();
        IProject project = javaProject.getProject();

        if (!IProjectUtils.hasEgapNature(project)) {

            if (!loggedWarningAboutProjectWithoutEgapNature) {
                /* One log message is sufficient! */
                EgapPlugin.logInfo("The de.jaculon.egap quickfixes are disabled as the project '" + project.getName()
                        + "' does not have the de.jaculon.egap nature. To enable the de.jaculon.egap quickfixes "
                        + "click the 'Add Egap Nature' button in the projects context menu.");
                loggedWarningAboutProjectWithoutEgapNature = true;
            }

            return null;
        }

        /*
         * Simple benchmark to ensure that we perform a quickfix not a slooow
         * fix !
         */
        long now = System.currentTimeMillis();

        EgapPlugin egapPlugin = EgapPlugin.getEgapPlugin();
        List<EgapQuickFix> egapQuickfixes = egapPlugin.getQuickfixes();

        List<IJavaCompletionProposal> proposals = ListUtils.newLinkedList();

        int nrOfEnabledQuickFixes = 0;
        for (EgapQuickFix quickFix : egapQuickfixes) {
            nrOfEnabledQuickFixes++;
            /* Every quickfix can contribute a proposal. */
            try {
                quickFix.addProposals(context, proposals);
            } catch (Exception e) {
                EgapPlugin.log(IStatus.ERROR, e.getMessage(), e);
            }
        }

        long then = System.currentTimeMillis();
        long diff = then - now;

        String message = "Egap QuickFix took " + diff + " ms, " + nrOfEnabledQuickFixes + " fixes enabled.";
        if (diff > 50) {
            EgapPlugin.logWarning(message);
        } else {
            EgapPlugin.logInfo(message);
        }

        if (proposals.isEmpty()) {
            return null;
        }

        return proposals.toArray(new IJavaCompletionProposal[proposals.size()]);
    }

}
