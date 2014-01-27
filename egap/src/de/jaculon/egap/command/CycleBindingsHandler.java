package de.jaculon.egap.command;

import static de.jaculon.egap.utils.CollectionUtils.getFirst;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;

import de.jaculon.egap.EgapPlugin;
import de.jaculon.egap.guice.GuiceIndex;
import de.jaculon.egap.guice.injection_point.IInjectionPoint;
import de.jaculon.egap.guice.injection_point.InjectionPointDao;
import de.jaculon.egap.guice.statements.BindingDefinition;
import de.jaculon.egap.selection.ICompilationUnitSelection;
import de.jaculon.egap.source_reference.SourceCodeReference;
import de.jaculon.egap.utils.ICompilationUnitSelectionUtils;
import de.jaculon.egap.utils.ListUtils;
import de.jaculon.egap.widgets.Widgets;

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
		        EgapPlugin.logInfo("No binding definition found for injection point " + injectionPoint);
			}
		}

	}
}