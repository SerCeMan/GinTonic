package ru.naumen.gintonic.command;

import static ru.naumen.gintonic.utils.CollectionUtils.getFirst;

import java.util.List;

import ru.naumen.gintonic.GinTonicPlugin;
import ru.naumen.gintonic.guice.injection.IInjectionPoint;
import ru.naumen.gintonic.guice.statements.BindingDefinition;
import ru.naumen.gintonic.project.navigate.selection.ICompilationUnitSelection;
import ru.naumen.gintonic.project.source.references.SourceCodeReference;
import ru.naumen.gintonic.utils.ListUtils;
import ru.naumen.gintonic.widgets.Widgets;

/**
 * Jumps from an {@link IInjectionPoint} to its binding definition(s).
 * 
 * @author tmajunke
 */
public class CycleBindingsHandler extends BaseBindingHandler {

	private BindingNavigationCycle navigationCycle;

    @Override
    protected void makeCommand(ICompilationUnitSelection compilationUnitSelection,
            SourceCodeReference currentCodeLocation, IInjectionPoint injectionPoint) {
        List<BindingDefinition> bindingDefinitions = guiceIndex.getBindingDefinitions(injectionPoint);
        if (!bindingDefinitions.isEmpty()) {
            BindingDefinition bindingDefinition = null;
            if(bindingDefinitions.size() > 1) {
                bindingDefinition = Widgets.showUserSelectWithSelected(bindingDefinitions, getFirst(bindingDefinitions));
                if(bindingDefinition == null) {
                    return;
                }
            } 
            if(bindingDefinitions.size() > 0) {
                bindingDefinition = getFirst(bindingDefinitions);
            }
            navigationCycle = new BindingNavigationCycle(currentCodeLocation, ListUtils.newArrayList(bindingDefinition));
            navigationCycle.jumpToNext();
        } else {
            GinTonicPlugin.logInfo("No binding definition found for injection point " + injectionPoint);
        }
    }
}