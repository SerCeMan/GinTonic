package de.jaculon.egap.command;

import static de.jaculon.egap.utils.CollectionUtils.getFirst;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;

import de.jaculon.egap.cu_selection.ICompilationUnitSelection;
import de.jaculon.egap.cu_selection.ICompilationUnitSelectionUtils;
import de.jaculon.egap.guice.GuiceIndex;
import de.jaculon.egap.guice.injection_point.IInjectionPoint;
import de.jaculon.egap.guice.injection_point.InjectionPointDao;
import de.jaculon.egap.guice.statements.BindingDefinition;
import de.jaculon.egap.guice.statements.LinkedBindingStatement;
import de.jaculon.egap.select_and_reveal.SelectAndReveal;

/**
 * Go To Impl handler
 * 
 * @author Sergey Tselovalnikov
 * @since Jan 16, 2014
 */
public class GoToImplHandler extends BaseHandler {

    private InjectionPointDao injectionPointDao;
    private GuiceIndex guiceIndex;

    @Override
    protected void handleEvent(ExecutionEvent event) {
        if (this.guiceIndex == null) {
            this.guiceIndex = GuiceIndex.get();
        }
        if (this.injectionPointDao == null) {
            injectionPointDao = new InjectionPointDao();
        }
        goToImpl(event);
    }

    private void goToImpl(ExecutionEvent event) {
        ICompilationUnitSelection unitSelection = ICompilationUnitSelectionUtils.getCompilationUnitSelection();
        if (unitSelection == null) {
            return;
        }

        IInjectionPoint injectionPoint = injectionPointDao.findInjectionPointByTextSelection(
                unitSelection.getICompilationUnit(), 
                unitSelection.getITextSelection());

        if (injectionPoint != null) {
            BindingDefinition bindingDefinition = getFirst(guiceIndex.getBindingDefinitions(injectionPoint));
            IProject project = unitSelection.getICompilationUnit().getResource().getProject();
            if (bindingDefinition instanceof LinkedBindingStatement) {
                LinkedBindingStatement binding = (LinkedBindingStatement) bindingDefinition;
                String implType = binding.getImplType();
                SelectAndReveal.selectAndRevealType(implType, project);
            } else {
                SelectAndReveal.selectAndRevealType(bindingDefinition.getBoundType(), project);
            }
        }
    }
}
