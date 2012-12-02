package de.jaculon.egap.command;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.jaculon.egap.EgapPlugin;
import de.jaculon.egap.cu_selection.ICompilationUnitSelection;
import de.jaculon.egap.cu_selection.ICompilationUnitSelectionUtils;
import de.jaculon.egap.guice.GuiceIndex;
import de.jaculon.egap.guice.injection_point.IInjectionPoint;
import de.jaculon.egap.guice.injection_point.InjectionPointDao;
import de.jaculon.egap.guice.statements.BindingDefinition;
import de.jaculon.egap.project_resource.IProjectResourceUtils;
import de.jaculon.egap.project_resource.ProjectResource;

/**
 * Jumps from an {@link IInjectionPoint} to its binding definition(s).
 * 
 * @author tmajunke
 */
public class CycleBindingsHandler extends AbstractHandler {

	private InjectionPointDao injectionPointDao;
	private GuiceIndex guiceIndex;
	
	public void setInjectionPointDao(InjectionPointDao injectionPointDao) {
		this.injectionPointDao = injectionPointDao;
	}

	public void setGuiceIndex(GuiceIndex guiceIndex) {
		this.guiceIndex = guiceIndex;
	}

	private BindingNavigationCycle navigationCycle;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (this.guiceIndex == null) {
			this.guiceIndex = GuiceIndex.get();
		}
		if(this.injectionPointDao == null){
			injectionPointDao = new InjectionPointDao();
		}
		
		cycle();

		return null;
	}

	private void cycle() {

		ICompilationUnitSelection compilationUnitSelection = ICompilationUnitSelectionUtils.getCompilationUnitSelection();

		if (compilationUnitSelection == null) {
			return;
		}

		ProjectResource currentCodeLocation = IProjectResourceUtils.createProjectResource(
				compilationUnitSelection.getICompilationUnit(),
				compilationUnitSelection.getITextSelection());

		if (navigationCycle != null) {
			boolean couldJump = navigationCycle.jumpToFollower(currentCodeLocation);
			if (couldJump) {
				return;
			}
		}

		IInjectionPoint injectionPoint = injectionPointDao.findInjectionPointByTextSelection(
				compilationUnitSelection.getICompilationUnit(),
				compilationUnitSelection.getITextSelection());

		if (injectionPoint != null) {

			List<BindingDefinition> bindingDefinitions = guiceIndex.getBindingDefinitionsFor(injectionPoint);

			if (!bindingDefinitions.isEmpty()) {
				navigationCycle = new BindingNavigationCycle(
						currentCodeLocation,
						bindingDefinitions);
				navigationCycle.jumpToNext();
			}
			else {
				EgapPlugin.logInfo("No binding definition found for injection point "
						+ injectionPoint.toString());
			}
		}

	}

}