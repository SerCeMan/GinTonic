package de.jaculon.egap.guice.injection_point;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import de.jaculon.egap.guice.GuiceConstants;
import de.jaculon.egap.guice.annotations.GuiceAnnotation;
import de.jaculon.egap.utils.ASTNodeUtils;
import de.jaculon.egap.utils.AnnotationList;
import de.jaculon.egap.utils.EclipseUtils;
import de.jaculon.egap.utils.FieldDeclarationUtils;
import de.jaculon.egap.utils.ListUtils;
import de.jaculon.egap.utils.MethodDeclarationUtils;
import de.jaculon.egap.utils.StringUtils;

/**
 * Methods for finding {@link IInjectionPoint}s.
 *
 * @author tmajunke
 */
public class InjectionPointDao {

	/**
	 * Returns all {@link InjectionPoint}s for the given compilation unit. The
	 * superclasses of the given compilation unit are also included in the
	 * returned list. Returns an empty list if no {@link InjectionPoint}s could
	 * be found.
	 */
	public List<InjectionPoint> findAllByICompilationUnit(
			ICompilationUnit compilationUnit) throws JavaModelException {
		final List<InjectionPoint> injectionPoints = new ArrayList<InjectionPoint>(
				30);

		List<IType> types = new ArrayList<IType>(5);

		IType primaryType = compilationUnit.findPrimaryType();
		types.add(primaryType);

		ITypeHierarchy supertypeHierarchy = primaryType.newSupertypeHierarchy(null);

		IType[] allSuperClasses = supertypeHierarchy.getAllSuperclasses(primaryType);
		for (IType iType : allSuperClasses) {
			String typeQualified = iType.getFullyQualifiedName();
			if (typeQualified.equals("java.lang.Object")) {
				continue;
			}
			types.add(iType);
		}

		for (IType iType : types) {
			ITypeRoot typeRootOfType = iType.getTypeRoot();
			findInjectionPoints(typeRootOfType, injectionPoints);
		}

		return injectionPoints;
	}

	private void findInjectionPoints(ITypeRoot typeRoot,
			final List<InjectionPoint> injectionPoints) {
		final CompilationUnit compilationUnit = SharedASTProvider.getAST(
				typeRoot,
				SharedASTProvider.WAIT_YES,
				null);
		compilationUnit.accept(new ASTVisitor(false) {

			private String identifier;

			@Override
			public boolean visit(FieldDeclaration fieldDeclaration) {

				/* We can skip static fields as they cannot be injected. */
				boolean isStatic = FieldDeclarationUtils.isStatic(fieldDeclaration);
				if (isStatic) {
					return false;
				}

				fieldDeclaration.accept(new ASTVisitor() {

					@Override
					public boolean visit(VariableDeclarationFragment node) {
						SimpleName name = node.getName();
						identifier = name.getIdentifier();
						return false;
					}

				});

				InjectionPoint injectionPoint = analyzeForInjectionPoint(
						fieldDeclaration,
						compilationUnit,
						identifier);

				if (injectionPoint != null) {
					injectionPoints.add(injectionPoint);
				}

				identifier = null;

				return false;
			}

		});
	}

	/**
	 * Returns the currently selected {@link IInjectionPoint} or null.
	 */
	public IInjectionPoint findCurrentlySelectedInjectionPoint() {

		IEditorPart editorPart = EclipseUtils.getActiveEditor();

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

		return findInjectionPointByTextSelection(
				icompilationUnit,
				currentSelection);
	}

	/**
	 * Returns the {@link IInjectionPoint} that the textSelection refers to or
	 * null.
	 */
	public IInjectionPoint findInjectionPointByTextSelection(
			ICompilationUnit iCompilationUnit, ITextSelection textSelection) {
		IJavaElement selectedJavaElement;
		try {
			selectedJavaElement = iCompilationUnit.getElementAt(textSelection.getOffset());
		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		}
		int elementType = selectedJavaElement.getElementType();

		/**
		 * Here we can perform a quick check on the IJavaElement if the
		 * currently selected element can be an injection point. Trying to avoid the parsing.
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
		 */
		if (!(elementType == IJavaElement.FIELD || elementType == IJavaElement.METHOD)) {
			return null;
		}

		CompilationUnit compilationUnit = SharedASTProvider.getAST(
				iCompilationUnit,
				SharedASTProvider.WAIT_YES,
				null);

		int length = textSelection.getLength();
		int offset = textSelection.getOffset();

		ASTNode coveredNode = NodeFinder.perform(
				compilationUnit,
				offset,
				length);

		IInjectionPoint injectionPoint = findByAstNode(
				coveredNode,
				compilationUnit);

		return injectionPoint;
	}

	/**
	 * Returns the given {@link ASTNode} as a {@link IInjectionPoint} if the
	 * {@link ASTNode}
	 *
	 * <ul>
	 *
	 * <li>is an identifier of a field declaration and the field declaration is
	 * annoted with {@link GuiceConstants#ANNOTATION_INJECT}. In this case
	 * the returned type is a {@link InjectionPoint}.</li>
	 * <li>is a parameter of a method declaration and the method declaration is
	 * annoted with {@link GuiceConstants#PROVIDES}. In this case the
	 * returned type is a {@link ProviderMethod}.</li>
	 * </ul>
	 *
	 * @param astNode the {@link ASTNode} which maybe is an
	 *            {@link IInjectionPoint}
	 * @param compilationUnit the compilationUnit of the {@link ASTNode}
	 * @return the {@link IInjectionPoint} or null.
	 */
	public IInjectionPoint findByAstNode(ASTNode astNode,
			CompilationUnit compilationUnit) {

		if (!(astNode instanceof Name)) {
			return null;
		}

		Name name = (Name) astNode;
		IInjectionPoint injectionPoint = getGuiceFieldDeclarationIfFieldDeclaration(
				name,
				compilationUnit);
		if (injectionPoint != null) {
			return injectionPoint;
		}

		ProviderMethod providerMethod = getProviderMethod(name);

		return providerMethod;
	}

	public InjectionPoint getGuiceFieldDeclarationIfFieldDeclaration(
			ASTNode astNode, CompilationUnit astRoot) {
		if (!(astNode instanceof Name)) {
			return null;
		}
		Name name = (Name) astNode;
		final String variableName = name.getFullyQualifiedName();

		FieldDeclaration fieldDeclaration = ASTNodeUtils.getFieldDeclaration(name);
		if (fieldDeclaration != null) {
			InjectionPoint guiceFieldDeclaration = analyzeForInjectionPoint(
					fieldDeclaration,
					astRoot,
					variableName);

			return guiceFieldDeclaration;
		}
		return null;
	}

	private ProviderMethod getProviderMethod(Name name) {
		ASTNode parentNode = name.getParent();
		if (parentNode instanceof SingleVariableDeclaration) {
			SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) parentNode;
			boolean isProviderMethod = ASTNodeUtils.isProviderMethod(singleVariableDeclaration.getParent());

			if (isProviderMethod) {
				@SuppressWarnings("unchecked")
				AnnotationList markerAnnotationList = getAnnotationList(singleVariableDeclaration.modifiers());
				Type type = singleVariableDeclaration.getType();
				GuiceAnnotation guiceAnnotation = markerAnnotationList.getGuiceAnnotation();
				return new ProviderMethod(
						type.resolveBinding(),
						guiceAnnotation,
						name.getFullyQualifiedName());
			}
			return null;

		}
		ASTNode parentParent = parentNode.getParent();
		if(parentParent instanceof FieldDeclaration) {
			FieldDeclaration fieldDeclaration = (FieldDeclaration) parentParent;
			
				@SuppressWarnings("unchecked")
				AnnotationList markerAnnotationList = getAnnotationList(fieldDeclaration.modifiers());
				Type type = fieldDeclaration.getType();
				GuiceAnnotation guiceAnnotation = markerAnnotationList.getGuiceAnnotation();
				return new ProviderMethod(
						type.resolveBinding(),
						guiceAnnotation,
						name.getFullyQualifiedName());
		}
		return null;
	}

	private AnnotationList getAnnotationList(List<ASTNode> modifiers) {
		List<Annotation> annotations = ListUtils.newArrayListWithCapacity(modifiers.size());

		for (ASTNode modifier : modifiers) {
			if (modifier instanceof Annotation) {
				annotations.add((Annotation) modifier);
			}
		}
		return new AnnotationList(annotations);
	}

	@SuppressWarnings("unchecked")
	private InjectionPoint analyzeForInjectionPoint(
			FieldDeclaration fieldDeclaration, CompilationUnit compilationUnit,
			String fieldName) {

		List<ASTNode> modifiers = fieldDeclaration.modifiers();

		AnnotationList annotationList = ASTNodeUtils.getAnnotationList(modifiers);

		if (annotationList.containsInjectType()) {
			Type type = fieldDeclaration.getType();
			GuiceAnnotation guiceAnnotation = annotationList.getGuiceAnnotation();
			return new InjectionPoint(
					type.resolveBinding(),
					guiceAnnotation,
					fieldName,
					fieldDeclaration,
					InjectionIsAttachedTo.FIELD);
		}

		/*
		 * Check the @Inject constructor if we can find a parameter with the
		 * same name as the selected field.
		 */
		MethodDeclaration constructor = MethodDeclarationUtils.getConstructorAnnotatedWithInject(compilationUnit);
		if (constructor != null) {
			SingleVariableDeclaration variableDeclaration = MethodDeclarationUtils.getVariableDeclarationsByName(
					constructor,
					fieldName);
			if (variableDeclaration != null) {
				annotationList = ASTNodeUtils.getAnnotationList(variableDeclaration.modifiers());
				Type type = variableDeclaration.getType();
				GuiceAnnotation guiceAnnotation = annotationList.getGuiceAnnotation();
				return new InjectionPoint(
						type.resolveBinding(),
						guiceAnnotation,
						fieldName,
						fieldDeclaration,
						InjectionIsAttachedTo.CONSTRUCTOR);
			}
		}

		/*
		 * Check if a guicified setter method exists.
		 */
		String setterMethodName = "set" + StringUtils.capitalize(fieldName);

		MethodDeclaration setter = ASTNodeUtils.getMethodByNameExpectSingleMethod(
				compilationUnit,
				setterMethodName);
		if (setter != null) {
			annotationList = ASTNodeUtils.getAnnotationList(setter.modifiers());
			if (annotationList.containsInjectType()) {
				Type type = fieldDeclaration.getType();
				GuiceAnnotation guiceAnnotation = annotationList.getGuiceAnnotation();
				return new InjectionPoint(
						type.resolveBinding(),
						guiceAnnotation,
						fieldName,
						fieldDeclaration,
						InjectionIsAttachedTo.SETTER);
			}
		}
		return null;
	}

}
