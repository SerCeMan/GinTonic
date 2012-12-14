package de.jaculon.egap.command;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jface.text.ITextSelection;

import de.jaculon.egap.EgapPlugin;
import de.jaculon.egap.cu_selection.ICompilationUnitSelection;
import de.jaculon.egap.cu_selection.ICompilationUnitSelectionUtils;
import de.jaculon.egap.guice.GuiceIndex;
import de.jaculon.egap.guice.injection_point.IInjectionPoint;
import de.jaculon.egap.guice.injection_point.InjectionPointDao;
import de.jaculon.egap.guice.statements.BindingDefinition;
import de.jaculon.egap.source_reference.SourceCodeReference;
import de.jaculon.egap.utils.ICompilationUnitUtils;
import de.jaculon.egap.utils.StringUtils;

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

		SourceCodeReference currentCodeLocation = createSourceCodeReference(
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

			List<BindingDefinition> bindingDefinitions = guiceIndex.getBindingDefinitions(injectionPoint);

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
	
	private SourceCodeReference createSourceCodeReference(
			ICompilationUnit icompilationUnit, ITextSelection textSelection) {
		SourceCodeReference codeReference = new SourceCodeReference();

		IResource resource = icompilationUnit.getResource();
		IProject project = resource.getProject();
		codeReference.setProjectName(project.getName());

		List<String> srcFolderPath = ICompilationUnitUtils.getSrcFolderPathComponents(icompilationUnit);
		codeReference.setSrcFolderPathComponents(srcFolderPath);

		IPackageFragment parent = (IPackageFragment) icompilationUnit.getParent();
		String packageDotSeparated = parent.getElementName();
		List<String> packageAsList = StringUtils.split('.', packageDotSeparated);
		codeReference.setPackageNameComponents(packageAsList);

		String typeName = ICompilationUnitUtils.getNameWithoutJavaExtension(icompilationUnit);
		codeReference.setPrimaryTypeName(typeName);

		if (textSelection != null) {
			int offset = textSelection.getOffset();
			codeReference.setOffset(offset);
			int length = textSelection.getLength();
			codeReference.setLength(length);
		}

		return codeReference;
	}

}