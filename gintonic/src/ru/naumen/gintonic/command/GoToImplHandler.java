package ru.naumen.gintonic.command;

import static ru.naumen.gintonic.utils.CollectionUtils.getFirst;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;

import ru.naumen.gintonic.guice.GuiceIndex;
import ru.naumen.gintonic.guice.injection.IInjectionPoint;
import ru.naumen.gintonic.guice.injection.InjectionPointDao;
import ru.naumen.gintonic.guice.statements.BindingDefinition;
import ru.naumen.gintonic.guice.statements.JustInTimeBindingStatement;
import ru.naumen.gintonic.guice.statements.LinkedBindingStatement;
import ru.naumen.gintonic.navigate.selection.ICompilationUnitSelection;
import ru.naumen.gintonic.project.files.SelectAndReveal;
import ru.naumen.gintonic.project.source.references.SourceCodeReference;
import ru.naumen.gintonic.utils.ICompilationUnitSelectionUtils;
import ru.naumen.gintonic.utils.ListUtils;
import ru.naumen.gintonic.widgets.Widgets;

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
            List<BindingDefinition> bindingDefinitions = guiceIndex.getBindingDefinitions(injectionPoint);
            BindingDefinition bindingDefinition = null;
            if(bindingDefinitions.size() > 1) {
                bindingDefinition = Widgets.showUserSelect(bindingDefinitions);
                if(bindingDefinition == null) {
                    return;
                }
            } else if(!bindingDefinitions.isEmpty()) {
                bindingDefinition = getFirst(bindingDefinitions);
            }
            if (bindingDefinition == null && !bindingDefinitions.isEmpty()) {
                // May be user press Cancel or Escape
                return;
            }
            String typeName = extractTypeName(injectionPoint, bindingDefinitions, bindingDefinition);
            if (typeName == null) {
                return;
            }
            IProject project = unitSelection.getICompilationUnit().getResource().getProject();
            goToImpl(project, typeName);
        }
    }

    private void goToImpl(IProject project, String typeName) {
        SelectAndReveal.selectAndRevealType(typeName, project);
    }

    private String extractTypeName(IInjectionPoint injectionPoint, List<BindingDefinition> bindingDefinitions,
            BindingDefinition bindingDefinition) {
        String typeName;
        if (bindingDefinitions.isEmpty()) {
            // Implementation class generated at runtime, e.g. GWT.create()
            typeName = injectionPoint.getTargetTypeBinding().getBinaryName();
        } else if (bindingDefinition instanceof LinkedBindingStatement) {
            LinkedBindingStatement binding = (LinkedBindingStatement) bindingDefinition;
            typeName = binding.getImplType();
        } else if (bindingDefinition instanceof JustInTimeBindingStatement) {
            SourceCodeReference currentCodeLocation = SourceCodeReference.createCurrent();
            if(currentCodeLocation == null) {
                return null;
            }
            BindingNavigationCycle navigationCycle = new BindingNavigationCycle(currentCodeLocation, ListUtils.newArrayList(bindingDefinition));
            navigationCycle.jumpToNext();
            return null;
        } else {
            typeName = bindingDefinition.getBoundType();
        }
        if (typeName == null && injectionPoint.getTargetTypeBinding() != null) {
            // Maybe class has not bindings, no annotation, but it injected and DI know about it
            typeName = injectionPoint.getTargetTypeBinding().getQualifiedName();
        }
        return typeName;
    }
}
