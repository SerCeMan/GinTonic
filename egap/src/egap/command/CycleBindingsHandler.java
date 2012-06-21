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
 * Jumps from an binding to the binding definition.
 * 
 * @author tmajunke
 */
public class CycleBindingsHandler extends AbstractHandler {

	private IProjectResource currentCodeLocation;

	/**
	 * The binding from where we started the navigation cycle.
	 */
	private IProjectResource binding;
	/**
	 * The binding definitions to the binding.
	 */
	private List<GuiceStatement> bindingDefinitions;

	private int index;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		long now = System.currentTimeMillis();
		try {
			cycle();
		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		}
		long then = System.currentTimeMillis();
		long elapsed = then - now;
		EgapPlugin.logInfo("cycle bindings took " + elapsed + " ms");

		return null;
	}

	private void cycle() throws JavaModelException {
		GuiceTypeInfo newBinding = findBinding();

		if (newBinding != null) {
			/* New guice type means we start a new navigation cycle! */
			bindingDefinitions = findBindingDefinitions(newBinding);
			binding = newBinding.getOrigin();
			index = 0;
			jumpToNextBindingDefinition();
		}
		else {
			/*
			 * Ok, maybe we are in a binding definition of our navigation cycle.
			 */
			boolean isCurrentCodeLocationABindingDefinition = isCurrentCodeLocationABindingDefinition();
			if (isCurrentCodeLocationABindingDefinition) {
				jumpToNextBindingDefinition();
			}
			else {
				/*
				 * Not a new binding, not a binding definition. We can do
				 * nothing!
				 */
			}

		}
	}

	private boolean isCurrentCodeLocationABindingDefinition() {
		int j = 0;
		for (IProjectResource bindingDefinition : bindingDefinitions) {
			if (bindingDefinition.getStartPosition().equals(
					currentCodeLocation.getStartPosition())) {
				if (bindingDefinition.getTypeNameFullyQualified().equals(
						currentCodeLocation.getTypeNameFullyQualified())) {
					if (bindingDefinition.getProjectName().equals(
							currentCodeLocation.getProjectName())) {
						index = j + 1;
						return true;
					}
				}
			}
			j++;
		}
		return false;
	}

	/**
	 * Jumps to the next binding definition or the binding from where we
	 * started.
	 */
	private void jumpToNextBindingDefinition() {

		if (bindingDefinitions.isEmpty()) {
			return;
		}

		IProjectResource jumpTarget = null;
		if (index < bindingDefinitions.size()) {
			jumpTarget = bindingDefinitions.get(index);
			index++;
		}
		else {
			/* The last target is from where we came. */
			jumpTarget = binding;
			index = 0;
		}

		IProjectResourceUtils.openEditorWithStatementDeclaration(jumpTarget);

	}

	/**
	 * Returns the guice binding based upon the current selection.
	 * 
	 * @return the guice binding based upon the current selection or null if it
	 *         is not a guice binding.
	 * @throws JavaModelException
	 */
	private GuiceTypeInfo findBinding() throws JavaModelException {

		IEditorPart editorPart = EditorUtils.getActiveEditor();

		if (editorPart == null | !(editorPart instanceof ITextEditor)) {
			return null;
		}
		final ITextEditor textEditor = (ITextEditor) editorPart;
		IEditorInput editorInput = textEditor.getEditorInput();

		ITypeRoot editorInputTypeRoot = JavaUI.getEditorInputTypeRoot(editorInput);

		if (!(editorInputTypeRoot instanceof ICompilationUnit)) {
			return null;
		}

		ICompilationUnit icompilationUnit = (ICompilationUnit) editorInputTypeRoot;

		ISelectionProvider selectionProvider = textEditor.getSelectionProvider();
		ISelection sel = selectionProvider.getSelection();
		if (!(sel instanceof ITextSelection)) {
			return null;
		}
		ITextSelection currentSelection = (ITextSelection) sel;
		int offset = currentSelection.getOffset();

		IJavaElement selectedJavaElement = icompilationUnit.getElementAt(offset);
		int elementType = selectedJavaElement.getElementType();

		/**
		 * Here we can perform a quick check on the IJavaElement if the
		 * currently selected element is a binding.
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
		 * A binding definition is of type 'IJavaElement.METHOD'
		 * bind(Date.class).annotatedWith(TimeBarCloses.class).toInstance(
		 * new Date(0, 0, 0, 11, 0, 0));
		 * 
		 * <pre>
		 */
		if (!(elementType == IJavaElement.FIELD || elementType == IJavaElement.METHOD)) {
			return null;
		}

		CompilationUnit compilationUnit = SharedASTProvider.getAST(
				editorInputTypeRoot,
				SharedASTProvider.WAIT_YES,
				null);

		int length = currentSelection.getLength();
		ASTNode coveredNode = findCoveredNode(compilationUnit, offset, length);

		currentCodeLocation = IProjectResourceUtils.createProjectResource(
				coveredNode,
				compilationUnit,
				icompilationUnit);

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

}
