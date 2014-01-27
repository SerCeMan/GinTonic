package ru.naumen.gintonic.command;

import static ru.naumen.gintonic.utils.CollectionUtils.getFirst;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;

import ru.naumen.gintonic.GinTonicPlugin;
import ru.naumen.gintonic.guice.GuiceIndex;
import ru.naumen.gintonic.guice.injection.IInjectionPoint;
import ru.naumen.gintonic.guice.injection.InjectionPointDao;
import ru.naumen.gintonic.guice.statements.BindingDefinition;
import ru.naumen.gintonic.navigate.selection.ICompilationUnitSelection;
import ru.naumen.gintonic.project.source.references.SourceCodeReference;
import ru.naumen.gintonic.utils.ICompilationUnitSelectionUtils;
import ru.naumen.gintonic.utils.ListUtils;
import ru.naumen.gintonic.widgets.Widgets;

/**
 * Jumps from an {@link IInjectionPoint} to its binding definition(s).
 * 
 * @author tmajunke
 */
public class CycleBindingsHandler extends BaseHandler {

	private InjectionPointDao injectionPointDao;
	private GuiceIndex guiceIndex;
	
	private BindingNavigationCycle navigationCycle;

	@Override
	protected void handleEvent(ExecutionEvent event) {
		if (this.guiceIndex == null) {
			this.guiceIndex = GuiceIndex.get();
		}
		if(this.injectionPointDao == null){
			injectionPointDao = new InjectionPointDao();
		}
		
		cycle();
	}

	private void cycle() {

		SourceCodeReference currentCodeLocation = SourceCodeReference.createCurrent();
		if (currentCodeLocation == null) {
		    return;
		}

		// Break standard feature
		/*if (navigationCycle != null) {
			boolean couldJump = navigationCycle.jumpToFollower(currentCodeLocation);
			if (couldJump) {
				return;
			}
		}*/

		ICompilationUnitSelection compilationUnitSelection = ICompilationUnitSelectionUtils.getCompilationUnitSelection();
		IInjectionPoint injectionPoint = injectionPointDao.findInjectionPointByTextSelection(
				compilationUnitSelection.getICompilationUnit(),
				compilationUnitSelection.getITextSelection());

		if (injectionPoint != null) {
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
}