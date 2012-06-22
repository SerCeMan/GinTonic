package egap.utils;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;


import egap.guice.ProjectResource;
import egap.guice.annotations.GuiceAnnotation;
import egap.guice.statements.GuiceStatement;

public class ASTNodeUtils {

	public static boolean isAssistedAnnotation(ASTNode node) {
		return isMarkerAnnotationAnnotatedWith(
				node,
				StringUtils.GUICE_ANNOTATION_ASSISTED);
	}

	/**
	 * Resolves the {@link ITypeBinding} for the Ast node.
	 * 
	 * @param coveringNode the ast node.
	 * @return the type binding or null if the node does not represent a guice
	 *         module.
	 */
	public static ITypeBinding getTypeBindingIfGuiceModuleCovered(ASTNode node) {
		if (node instanceof Name) {
			Name name = (Name) node;
			ITypeBinding guiceModuleSource = name.resolveTypeBinding();
			boolean isGuiceModule = ITypeBindingUtils.isGuiceModuleType(guiceModuleSource);
			if (isGuiceModule) {
				return guiceModuleSource;
			}
		}
		return null;
	}

	/**
	 * Resolves the {@link ITypeBinding} for the Ast node if the node is a
	 * {@link Name} and the type is <b>not</b> a Guice module.
	 * 
	 * @param coveringNode the ast node.
	 * @return the type binding or null if the node does represent a guice
	 *         module.
	 */
	public static ITypeBinding getTypeBindingIfNotAGuiceModule(ASTNode node) {
		if (node instanceof Name) {
			Name name = (Name) node;
			ITypeBinding guiceModuleSource = name.resolveTypeBinding();
			boolean isGuiceModule = ITypeBindingUtils.isGuiceModuleType(guiceModuleSource);
			if (!isGuiceModule) {
				return guiceModuleSource;
			}
		}
		return null;
	}

	public static void copyStartPositionAndLength(ASTNode astNode,
			GuiceStatement guiceStatement) {
		guiceStatement.setStartPosition(astNode.getStartPosition());
		guiceStatement.setLength(astNode.getLength());
	}

	public static MarkerAnnotationList getMarkerAnnotationList(
			List<ASTNode> modifiers) {
		List<MarkerAnnotation> markerAnnotations = ListUtils.newArrayListWithCapacity(modifiers.size());
		List<SingleMemberAnnotation> singleMemberAnnotations = ListUtils.newArrayListWithCapacity(modifiers.size());

		for (ASTNode modifier : modifiers) {
			if (modifier instanceof MarkerAnnotation) {
				markerAnnotations.add((MarkerAnnotation) modifier);
			}
			else if (modifier instanceof SingleMemberAnnotation) {
				/* Names.named("jack") */
				singleMemberAnnotations.add((SingleMemberAnnotation) modifier);
			}
		}
		return new MarkerAnnotationList(
				markerAnnotations,
				singleMemberAnnotations);
	}

	private static final class FindMethodByName extends ASTVisitor {
		private final String fieldAsSetter;
		private List<MethodDeclaration> methodDeclarations = ListUtils.newArrayList();

		private FindMethodByName(String fieldAsSetter) {
			this.fieldAsSetter = fieldAsSetter;
		}

		@Override
		public boolean visit(MethodDeclaration node) {
			SimpleName name = node.getName();
			String fullyQualifiedName = name.getFullyQualifiedName();
			if (fullyQualifiedName.equals(fieldAsSetter)) {
				methodDeclarations.add(node);
			}
			return true;
		}
	}

	private static final class ASTVisitorFindConstructor extends ASTVisitor {
		public List<MethodDeclaration> constructors = ListUtils.newArrayList();

		private ASTVisitorFindConstructor(boolean visitDocTags) {
			super(visitDocTags);
		}

		@Override
		public boolean visit(MethodDeclaration node) {
			if (isConstructor(node)) {
				constructors.add(node);
			}
			return true;
		}
	}

	/**
	 * Returns true if any of the given modifiers is a {@link MarkerAnnotation}
	 * with the given name.
	 * 
	 * @param modifiers the modifiers
	 * @param markerAnnotationFQ the fully qualified name of the marker
	 *            annotation
	 * @return true if any of the given modifiers is a {@link MarkerAnnotation}
	 *         with the given name, otherwise false.
	 */
	public static boolean isAnyAnnotatedWithMarkerAnnotation(
			List<ASTNode> modifiers, String markerAnnotationFQ) {
		for (ASTNode modifier : modifiers) {
			boolean foundAnnotation = ASTNodeUtils.isMarkerAnnotationAnnotatedWith(
					modifier,
					markerAnnotationFQ);
			if (foundAnnotation) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if the given node is a {@link MarkerAnnotation} and the
	 * qualified name of its type is equal to the given one.
	 * 
	 * @param node the node
	 * @param annotationFullyQualified the annotation name we compare
	 * 
	 * @return true if it is annotated with the given type otherwise false.
	 */
	public static boolean isMarkerAnnotationAnnotatedWith(ASTNode node,
			String annotationFullyQualified) {
		if (node instanceof MarkerAnnotation) {
			MarkerAnnotation markerAnnotation = (MarkerAnnotation) node;
			ITypeBinding typeBinding = markerAnnotation.resolveTypeBinding();

			String qualifiedName = typeBinding.getQualifiedName();
			if (qualifiedName.equals(annotationFullyQualified)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if the given node is an identifier of a {@link FieldDeclaration}.
	 * If it is then it is returned, otherwise null is returned.
	 * 
	 * @param astNode the node
	 * @return the field declaration or null.
	 */
	public static FieldDeclaration getFieldDeclaration(ASTNode astNode) {
		if (!(astNode instanceof Name)) {
			return null;
		}

		Name name = (Name) astNode;
		ASTNode parentNode = name.getParent();

		if (!(parentNode.getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT)) {
			return null;
		}

		VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) parentNode;
		ASTNode parentParentNode = declarationFragment.getParent();

		if (parentParentNode instanceof FieldDeclaration) {
			FieldDeclaration fieldDeclaration = (FieldDeclaration) parentParentNode;
			return fieldDeclaration;

		}

		return null;
	}

	/**
	 * Returns the parent of the given node if it is a {@link TypeDeclaration},
	 * otherwise null.
	 */
	public static TypeDeclaration getTypeDeclaration(ASTNode astNode) {
		ASTNode parentNode = astNode.getParent();
		if (parentNode instanceof TypeDeclaration) {
			TypeDeclaration typeDeclaration = (TypeDeclaration) parentNode;
			return typeDeclaration;
		}
		return null;
	}

	/**
	 * Returns the <b>first</b> constructor whose parameters contain at least
	 * one paramter with an @Assisted annotation. Null if noone could be found.
	 */
	public static MethodDeclaration getConstructorWithAtLeastOneAssistedAnnotation(
			ASTNode astNode) {
		ASTVisitorFindConstructor visitor = new ASTVisitorFindConstructor(false);
		astNode.accept(visitor);
		List<MethodDeclaration> constructors = visitor.constructors;

		for (MethodDeclaration constructor : constructors) {
			@SuppressWarnings("unchecked")
			List<SingleVariableDeclaration> parameters = constructor.parameters();
			for (SingleVariableDeclaration singleVariableDeclaration : parameters) {
				@SuppressWarnings("unchecked")
				List<ASTNode> modifiers = singleVariableDeclaration.modifiers();
				if (isAnyAnnotatedWithMarkerAnnotation(
						modifiers,
						StringUtils.GUICE_ANNOTATION_ASSISTED)) {
					return constructor;
				}
			}
		}

		return null;
	}

	/**
	 * Returns the constructor with @Inject marker annotation or null if there
	 * is noone (Note: There may only be one constructor with @Inject, see
	 * http://code.google.com/p/google-guice/wiki/InjectionPoints)
	 * 
	 * @param compilationUnit the compilation unit we check for the constructor.
	 * @return the constructor with @Inject marker annotation or null if there
	 *         is noone.
	 */
	public static MethodDeclaration getConstructorAnnotatedWithInject(
			CompilationUnit compilationUnit) {
		MethodDeclaration constructor = getConstructorWithAtLeastOneMarkerAnnotation(
				compilationUnit,
				StringUtils.GUICE_ANNOTATION_INJECT);
		return constructor;
	}

	/**
	 * Returns the <b>first</b> constructor which is annotated with the given
	 * marker annotation. Null if noone could be found.
	 */
	public static MethodDeclaration getConstructorWithAtLeastOneMarkerAnnotation(
			ASTNode astNode, String markerAnnotationFQ) {
		ASTVisitorFindConstructor visitor = new ASTVisitorFindConstructor(false);
		astNode.accept(visitor);
		List<MethodDeclaration> constructors = visitor.constructors;

		for (MethodDeclaration constructor : constructors) {
			@SuppressWarnings("unchecked")
			List<ASTNode> modifiers = constructor.modifiers();
			if (ASTNodeUtils.isAnyAnnotatedWithMarkerAnnotation(
					modifiers,
					markerAnnotationFQ)) {
				return constructor;
			}
		}

		return null;
	}

	/**
	 * Returns true if the given {@link ASTNode} is a constructor.
	 * 
	 * @param node the node
	 * @return true if the given {@link ASTNode} is a constructor, otherwise
	 *         false.
	 */
	public static boolean isConstructor(ASTNode node) {
		if (node instanceof MethodDeclaration) {
			MethodDeclaration methodDecl = (MethodDeclaration) node;
			return methodDecl.isConstructor();
		}
		return false;
	}

	public static List<MethodDeclaration> getMethodByName(
			CompilationUnit astRoot, final String fieldAsSetter) {

		FindMethodByName methodByName = new FindMethodByName(fieldAsSetter);
		astRoot.accept(methodByName);
		List<MethodDeclaration> methodDeclarations = methodByName.methodDeclarations;

		return methodDeclarations;
	}

	public static MethodDeclaration getMethodByNameExpectSingleMethod(
			CompilationUnit astRoot, final String fieldAsSetter) {
		List<MethodDeclaration> methodByName = getMethodByName(
				astRoot,
				fieldAsSetter);
		if (methodByName.size() > 0) {
			MethodDeclaration methodDeclaration = methodByName.get(0);
			return methodDeclaration;
		}
		return null;
	}

	public static GuiceFieldDeclaration getGuiceFieldDeclarationIfFieldDeclaration(
			ProjectResource origin, ASTNode astNode, CompilationUnit astRoot) {
		if (!(astNode instanceof Name)) {
			return null;
		}
		Name name = (Name) astNode;
		final String variableName = name.getFullyQualifiedName();

		FieldDeclaration fieldDeclaration = ASTNodeUtils.getFieldDeclaration(name);
		if (fieldDeclaration != null) {
			GuiceFieldDeclaration guiceFieldDeclaration = FieldDeclarationUtils.getTypeIfAnnotatedWithInject(
					origin,
					fieldDeclaration,
					astRoot,
					variableName);

			return guiceFieldDeclaration;
		}
		return null;
	}
	
	public static GuiceTypeInfo getGuiceTypeInfoIfFieldDeclarationTypeDeclarationOrProviderMethod(
			ASTNode astNode, CompilationUnit astRoot,
			ICompilationUnit icompilationUnit) {

		if (!(astNode instanceof Name)) {
			return null;
		}

		ProjectResource origin = IProjectResourceUtils.createProjectResource(
				astNode,
				astRoot,
				icompilationUnit);

		Name name = (Name) astNode;
		GuiceFieldDeclaration guiceFieldDeclaration = getGuiceFieldDeclarationIfFieldDeclaration(
				origin,
				name,
				astRoot);
		if (guiceFieldDeclaration != null) {
			return guiceFieldDeclaration;
		}

		TypeDeclaration typeDeclaration = ASTNodeUtils.getTypeDeclaration(astNode);
		if (typeDeclaration != null) {
			ITypeBinding typeBinding = typeDeclaration.resolveBinding();
			return new GuiceTypeInfo(origin, typeBinding);
		}

		GuiceProviderMethodVar guicyProviderMethodVar = getGuiceTypeInfoIfVariableDeclOfProviderMethod(
				origin,
				name);
		if (guicyProviderMethodVar != null) {
			return guicyProviderMethodVar;
		}

		return guicyProviderMethodVar;
	}

	public static GuiceProviderMethodVar getGuiceTypeInfoIfVariableDeclOfProviderMethod(
			ProjectResource origin, Name name) {
		ASTNode parentNode = name.getParent();
		if (parentNode instanceof SingleVariableDeclaration) {
			SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) parentNode;
			boolean isProviderMethod = ASTNodeUtils.isProviderMethod(singleVariableDeclaration.getParent());

			if (isProviderMethod) {
				@SuppressWarnings("unchecked")
				MarkerAnnotationList markerAnnotationList = getMarkerAnnotationList(singleVariableDeclaration.modifiers());
				Type type = singleVariableDeclaration.getType();
				GuiceAnnotation guiceAnnotation = markerAnnotationList.getGuiceAnnotation();
				return new GuiceProviderMethodVar(
						origin,
						type.resolveBinding(),
						guiceAnnotation,
						name.getFullyQualifiedName());
			}
			return null;

		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static boolean isProviderMethod(ASTNode astNode) {
		if (astNode instanceof MethodDeclaration) {
			MethodDeclaration methodDeclaration = (MethodDeclaration) astNode;

			List<ASTNode> modifiers = methodDeclaration.modifiers();
			MarkerAnnotationList markerAnnotationList = ASTNodeUtils.getMarkerAnnotationList(modifiers);
			if (markerAnnotationList.containsProvidesType()) {
				return true;
			}
		}
		return false;
	}

}
