package ru.naumen.gintonic.command;

import static ru.naumen.gintonic.utils.CollectionUtils.getFirst;

import java.util.List;

import org.eclipse.core.resources.IProject;

import ru.naumen.gintonic.guice.injection.IInjectionPoint;
import ru.naumen.gintonic.guice.statements.BindingDefinition;
import ru.naumen.gintonic.guice.statements.JustInTimeBindingStatement;
import ru.naumen.gintonic.project.files.SelectAndReveal;
import ru.naumen.gintonic.project.navigate.selection.ICompilationUnitSelection;
import ru.naumen.gintonic.project.source.references.SourceCodeReference;
import ru.naumen.gintonic.utils.BindingUtils;
import ru.naumen.gintonic.utils.ListUtils;
import ru.naumen.gintonic.widgets.Widgets;

/**
 * Go To Impl handler
 * 
 * @author Sergey Tselovalnikov
 * @since Jan 16, 2014
 */
public class GoToImplHandler extends BaseBindingHandler {

    @Override
    protected void makeCommand(ICompilationUnitSelection unitSelection,
            SourceCodeReference currentCodeLocation, IInjectionPoint injectionPoint) {
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
        if (bindingDefinition instanceof JustInTimeBindingStatement) {
            BindingNavigationCycle navigationCycle = new BindingNavigationCycle(currentCodeLocation, ListUtils.newArrayList(bindingDefinition));
            navigationCycle.jumpToNext();
            return;
        }
        String typeName = BindingUtils.extractTypeName(injectionPoint, bindingDefinition);
        if (typeName == null) {
            return;
        }
        IProject project = unitSelection.getICompilationUnit().getResource().getProject();
        SelectAndReveal.selectAndRevealType(typeName, project);
    }
}
