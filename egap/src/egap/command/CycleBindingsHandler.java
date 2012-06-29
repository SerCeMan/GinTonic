package egap.command;

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

import egap.EgapPlugin;
import egap.guice.GuiceIndex;
import egap.guice.annotations.GuiceAnnotation;
import egap.guice.statements.GuiceStatement;
import egap.utils.ASTNodeUtils;
import egap.utils.EditorUtils;
import egap.utils.GuiceTypeInfo;
import egap.utils.GuiceTypeWithAnnotation;
import egap.utils.IProjectResource;
import egap.utils.IProjectResourceUtils;
import egap.utils.ITypeBindingUtils;

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

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		long now = System.currentTimeMillis();
		try {
			cycle();
		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		}finally{
			nullifyFields();
			long then = System.currentTimeMillis();
			long elapsed = then - now;
			EgapPlugin.logInfo("cycle bindings took " + elapsed + " ms");
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
			if(couldJump){
				return;
			}
		}

		GuiceTypeInfo binding = findBinding();

		if (binding != null) {
			/* We have a binding so we can create a new navigation cycle! */
			List<GuiceStatement> bindingDefinitions = findBindingDefinitions(binding);
			navigationCycle = new BindingNavigationCycle(bindingDefinitions, currentCodeLocation);
			navigationCycle.jumpToNext();
		}

	}

	private IProjectResource getCurrentCodeLocation() {

		IEditorPart editorPart = EditorUtils.getActiveEditor();

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
	 * Returns the guice binding based upon the current selection.
	 * 
	 * @return the guice binding based upon the current selection or null if it
	 *         is not a guice binding.
	 * @throws JavaModelException
	 */
	private GuiceTypeInfo findBinding() throws JavaModelException {

		IJavaElement selectedJavaElement = icompilationUnit.getElementAt(currentSelection.getOffset());
		int elementType = selectedJavaElement.getElementType();

		/**
		 * Here we can perform a quick check on the IJavaElement if the
		 * currently selected element is a binding. Trying to avoid the
		 * parsing.
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

		GuiceTypeInfo binding = ASTNodeUtils.getGuiceTypeInfoIfFieldDeclarationTypeDeclarationOrProviderMethod(
				coveredNode,
				compilationUnit,
				icompilationUnit);

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

	private List<GuiceStatement> findBindingDefinitions(GuiceTypeInfo binding) {
		List<GuiceStatement> projectResourcesToVisit;
		ITypeBinding typeBinding = binding.getTargetTypeBinding();
		GuiceIndex guiceIndex = GuiceIndex.get();
		ITypeBinding typeBindingWithoutProvider = ITypeBindingUtils.removeSurroundingProvider(typeBinding);
		if (binding instanceof GuiceTypeWithAnnotation) {
			GuiceTypeWithAnnotation annotatedThing = (GuiceTypeWithAnnotation) binding;
			GuiceAnnotation guiceAnnotation = annotatedThing.getGuiceAnnotation();
			projectResourcesToVisit = guiceIndex.getBindingsByTypeAndAnnotation(
					typeBindingWithoutProvider,
					guiceAnnotation);
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
