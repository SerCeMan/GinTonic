package de.jaculon.egap.command;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import de.jaculon.egap.EgapPlugin;
import de.jaculon.egap.guice.GuiceIndex;
import de.jaculon.egap.guice.annotations.GuiceAnnotation;
import de.jaculon.egap.guice.statements.GuiceStatement;
import de.jaculon.egap.utils.ASTNodeUtils;
import de.jaculon.egap.utils.EclipseUtils;
import de.jaculon.egap.utils.IAnnotatedInjectionPoint;
import de.jaculon.egap.utils.IInjectionPoint;
import de.jaculon.egap.utils.IProjectResource;
import de.jaculon.egap.utils.IProjectResourceUtils;
import de.jaculon.egap.utils.ITypeBindingUtils;


/**
 * Jumps from a binding to its binding definition(s).
 *
 * @author tmajunke
 */
public class CycleBindingsHandler extends AbstractHandler {

	private BindingNavigationCycle navigationCycle;

	/* Intermediate fields, which must be reset after execution. */
	private ITypeRoot editorInputTypeRoot;
	private ICompilationUnit icompilationUnit;
	private ITextSelection currentSelection;

	private long findCycleTook;
	private long findInjectionPointTook;
	private long findBindingDefinitionsTook;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		findCycleTook = 0;
		findInjectionPointTook = 0;
		findBindingDefinitionsTook = 0;

		long now = System.currentTimeMillis();
		try {
			cycle();
			long then = System.currentTimeMillis();
			findCycleTook = then - now;
			EgapPlugin.logInfo("jump to binding took " + findCycleTook
					+ " (findBindingDefinitions = "
					+ findBindingDefinitionsTook + ", findInjectionPoint = "
					+ findInjectionPointTook + " ms)");
		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		} finally {
			nullifyFields();
		}

		return null;
	}

	private void cycle() throws JavaModelException {

		IProjectResource currentCodeLocation = getCurrentCodeLocation();
		if (navigationCycle != null) {
			/*
			 * First we check if the current code location is contained in our
			 * navigation cycle.
			 */
			boolean couldJump = navigationCycle.jumpTo(currentCodeLocation);
			if (couldJump) {
				return;
			}
		}
		long now = System.currentTimeMillis();
		IInjectionPoint injectionPoint = findInjectionPoint();
		long then = System.currentTimeMillis();
		findInjectionPointTook = then - now;

		if (injectionPoint != null) {

			long now2 = System.currentTimeMillis();
			List<GuiceStatement> bindingDefinitions = findBindingDefinitions(injectionPoint);
			long then2 = System.currentTimeMillis();
			findBindingDefinitionsTook = then2 - now2;

			if (!bindingDefinitions.isEmpty()) {
				/*
				 * We have an injection point and a binding definition, now we
				 * can create a new navigation cycle!
				 */
				navigationCycle = new BindingNavigationCycle(
						bindingDefinitions,
						currentCodeLocation);
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

	/**
	 * Returns the injection point based upon the currently selected ast node.
	 *
	 * @return the injection point based upon the current selection or null if
	 *         the current selected ast node is not a guice binding.
	 * @throws JavaModelException
	 */
	private IInjectionPoint findInjectionPoint() throws JavaModelException {

		IJavaElement selectedJavaElement = icompilationUnit.getElementAt(currentSelection.getOffset());
		int elementType = selectedJavaElement.getElementType();

		/**
		 * Here we can perform a quick check on the IJavaElement if the
		 * currently selected element is a binding. Trying to avoid the parsing.
		 *
		 * <pre>
		 * IJavaElement.FIELD
		 * @Inject
		 * private IPianoPlayer<Bar> jackThePianoPlayer;
		 *
		 * IJavaElement.METHOD
		 * @Provides
		 * private Customer provideCustomer(@Seed Long seed) {
		 *
		 * IJavaElement.METHOD
		 * @Inject
		 * public void setServableDrinks(Set<Drink> servableDrinks) {
		 *   this.servableDrinks = servableDrinks;
		 * }
		 *
		 * <pre>
		 *
		 *
		 */
		if (!(elementType == IJavaElement.FIELD || elementType == IJavaElement.METHOD)) {
			return null;
		}

		CompilationUnit compilationUnit = SharedASTProvider.getAST(
				editorInputTypeRoot,
				SharedASTProvider.WAIT_YES,
				null);

		int length = currentSelection.getLength();
		int offset = currentSelection.getOffset();
		ASTNode coveredNode = findCoveredNode(compilationUnit, offset, length);

		IInjectionPoint binding = ASTNodeUtils.getInjectionPoint(
				coveredNode,
				compilationUnit);

		return binding;

	}

	private ASTNode findCoveredNode(CompilationUnit compilationUnit,
			int offset, int length) {
		ASTNode coveredNode = NodeFinder.perform(
				compilationUnit,
				offset,
				length);
		return coveredNode;
	}

	private List<GuiceStatement> findBindingDefinitions(IInjectionPoint binding) {
		List<GuiceStatement> projectResourcesToVisit;
		ITypeBinding typeBinding = binding.getTargetTypeBinding();
		GuiceIndex guiceIndex = GuiceIndex.get();
		ITypeBinding typeBindingWithoutProvider = ITypeBindingUtils.removeSurroundingProvider(typeBinding);
		if (binding instanceof IAnnotatedInjectionPoint) {
			IAnnotatedInjectionPoint annotatedThing = (IAnnotatedInjectionPoint) binding;
			GuiceAnnotation guiceAnnotation = annotatedThing.getGuiceAnnotation();

			long now = System.currentTimeMillis();

			projectResourcesToVisit = guiceIndex.getBindingsByTypeAndAnnotation(
					typeBindingWithoutProvider,
					guiceAnnotation);

			long then = System.currentTimeMillis();
			long elapsed = then - now;
			System.out.println("getBindingsByTypeAndAnnotation took " + elapsed + " ms");
		}
		else {
			/* We only have the type. */
			projectResourcesToVisit = guiceIndex.getBindingsByType(typeBindingWithoutProvider);
		}
		return projectResourcesToVisit;
	}

	private void nullifyFields() {
		editorInputTypeRoot = null;
		icompilationUnit = null;
		currentSelection = null;
	}

}
