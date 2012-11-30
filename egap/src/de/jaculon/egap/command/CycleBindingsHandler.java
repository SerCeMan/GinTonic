package de.jaculon.egap.command;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import de.jaculon.egap.EgapPlugin;
import de.jaculon.egap.guice.GuiceIndex;
import de.jaculon.egap.guice.injection_point.IInjectionPoint;
import de.jaculon.egap.guice.injection_point.InjectionPointDao;
import de.jaculon.egap.guice.statements.BindingDefinition;
import de.jaculon.egap.project_resource.IProjectResource;
import de.jaculon.egap.project_resource.IProjectResourceUtils;
import de.jaculon.egap.utils.EclipseUtils;

/**
 * Jumps from an {@link IInjectionPoint} to its binding definition(s).
 *
 * @author tmajunke
 */
public class CycleBindingsHandler extends AbstractHandler {

	private InjectionPointDao injectionPointDao = new InjectionPointDao();

	private BindingNavigationCycle navigationCycle;

	/* Intermediate fields, which must be reset after execution. */
	private ITypeRoot editorInputTypeRoot;
	private ICompilationUnit icompilationUnit;
	private ITextSelection currentSelection;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			cycle();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			nullifyFields();
		}

		return null;
	}

	private void cycle(){

		IProjectResource currentCodeLocation = getCurrentCodeLocation();
		if (navigationCycle != null) {
			/*
			 * First we check if the current code location is contained in our
			 * navigation cycle.
			 */
			boolean couldJump = navigationCycle.jumpToFollower(currentCodeLocation);
			if (couldJump) {
				return;
			}
		}
		IInjectionPoint injectionPoint = injectionPointDao.findInjectionPointByTextSelection(
				icompilationUnit,
				currentSelection);

		if (injectionPoint != null) {

			GuiceIndex guiceIndex = GuiceIndex.get();
			List<BindingDefinition> bindingDefinitions = guiceIndex.getBindingDefinitionsFor(injectionPoint);

			if (!bindingDefinitions.isEmpty()) {
				/*
				 * We have an injection point and a binding definition, now we
				 * can create a new navigation cycle!
				 */
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

	private IProjectResource getCurrentCodeLocation() {

		IEditorPart editorPart = EclipseUtils.getActiveEditor();

		if (editorPart == null | !(editorPart instanceof ITextEditor)) {
			return null;
		}
		final ITextEditor textEditor = (ITextEditor) editorPart;
		IEditorInput editorInput = textEditor.getEditorInput();

		editorInputTypeRoot = JavaUI.getEditorInputTypeRoot(editorInput);

		if (!(editorInputTypeRoot instanceof ICompilationUnit)) {
			return null;
		}

		icompilationUnit = (ICompilationUnit) editorInputTypeRoot;

		ISelectionProvider selectionProvider = textEditor.getSelectionProvider();
		ISelection sel = selectionProvider.getSelection();
		if (!(sel instanceof ITextSelection)) {
			return null;
		}
		currentSelection = (ITextSelection) sel;

		return IProjectResourceUtils.createProjectResource(
				icompilationUnit,
				currentSelection);
	}

	private void nullifyFields() {
		editorInputTypeRoot = null;
		icompilationUnit = null;
		currentSelection = null;
	}

}
