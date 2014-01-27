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

import ru.naumen.gintonic.GinTonicPlugin;
import ru.naumen.gintonic.utils.IProjectUtils;
import ru.naumen.gintonic.utils.ListUtils;

public class GinTonicQuickFixProcessor implements IQuickFixProcessor {

    private boolean loggedWarningAboutProjectWithoutGinTonicNature;

    @Override
    public boolean hasCorrections(ICompilationUnit unit, int problemId) {
        return false;
    }

    @Override
    public IJavaCompletionProposal[] getCorrections(IInvocationContext context, IProblemLocation[] locations)
            throws CoreException {

        /**
         * The quick fixes are only enabled if the project has the
         * ru.naumen.gintonic nature.
         * 
         * Have to check this here, as the configuration in the plugin.xml:
         * 
         * <pre>
         * <with variable="projectNatures">
         * <equals value="ru.naumen.gintonic.GinTonicNature"/>
         * </with>
         * </pre>
         * 
         * does not do it!
         */
        ICompilationUnit compilationUnit = context.getCompilationUnit();
        IJavaElement parent = compilationUnit.getParent();
        IJavaProject javaProject = parent.getJavaProject();
        IProject project = javaProject.getProject();

        if (!IProjectUtils.hasGinTonicNature(project)) {

            if (!loggedWarningAboutProjectWithoutGinTonicNature) {
                /* One log message is sufficient! */
                GinTonicPlugin.logInfo("The ru.naumen.gintonic quickfixes are disabled as the project '" + project.getName()
                        + "' does not have the ru.naumen.gintonic nature. To enable the ru.naumen.gintonic quickfixes "
                        + "click the 'Add GinTonic Nature' button in the projects context menu.");
                loggedWarningAboutProjectWithoutGinTonicNature = true;
            }

            return null;
        }

        /*
         * Simple benchmark to ensure that we perform a quickfix not a slooow
         * fix !
         */
        long now = System.currentTimeMillis();

        GinTonicPlugin ginTonicPlugin = GinTonicPlugin.getGinTonicPlugin();
        List<GinTonicQuickFix> ginTonicQuickFixes = ginTonicPlugin.getQuickfixes();

        List<IJavaCompletionProposal> proposals = ListUtils.newLinkedList();

        int nrOfEnabledQuickFixes = 0;
        for (GinTonicQuickFix quickFix : ginTonicQuickFixes) {
            nrOfEnabledQuickFixes++;
            /* Every quickfix can contribute a proposal. */
            try {
                quickFix.addProposals(context, proposals);
            } catch (Exception e) {
                GinTonicPlugin.log(IStatus.ERROR, e.getMessage(), e);
            }
        }

        long then = System.currentTimeMillis();
        long diff = then - now;

        String message = "GinTonic QuickFix took " + diff + " ms, " + nrOfEnabledQuickFixes + " fixes enabled.";
        if (diff > 50) {
            GinTonicPlugin.logWarning(message);
        } else {
            GinTonicPlugin.logInfo(message);
        }

        if (proposals.isEmpty()) {
            return null;
        }

        return proposals.toArray(new IJavaCompletionProposal[proposals.size()]);
    }

}
