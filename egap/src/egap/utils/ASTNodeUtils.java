package egap.utils;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
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
			boolean isGuiceModule = ITypeBindingUtils.isGuiceAbstractModuleType(guiceModuleSource);
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
			boolean isGuiceModule = ITypeBindingUtils.isGuiceAbstractModuleType(guiceModuleSource);
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

	public static AnnotationList getAnnotationList(
			List<ASTNode> modifiers) {
		List<Annotation> annotations = ListUtils.newArrayListWithCapacity(modifiers.size());

		for (ASTNode modifier : modifiers) {
			if (modifier instanceof Annotation) {
				annotations.add((Annotation) modifier);
			}
		}
		return new AnnotationList(annotations);
	}

	private static final class FindMethodByName extends ASTVisitor {
		private final String fieldAsSetter;
		private List<MethodDeclaration> methodDeclarations = ListUtils.newArrayList();
		
		private FindMethodByName(String fieldAsSetter) {
			super(false);
			this.fieldAsSetter = fieldAsSetter;
		}

		@Override
		public boolean visit(MethodDeclaration node) {
			SimpleName name = node.getName();
			String fullyQualifiedName = name.getFullyQualifiedName();
			if (fullyQualifiedName.equals(fieldAsSetter)) {
				methodDeclarations.add(node);
			}
			return false;
		}
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
	private static boolean isMarkerAnnotationAnnotatedWith(ASTNode node,
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

	public static List<MethodDeclaration> getMethodByName(
			CompilationUnit astRoot, final String methodName) {

		FindMethodByName methodByName = new FindMethodByName(methodName);
		astRoot.accept(methodByName);
		List<MethodDeclaration> methodDeclarations = methodByName.methodDeclarations;

		return methodDeclarations;
	}

	public static MethodDeclaration getMethodByNameExpectSingleMethod(
			CompilationUnit astRoot, final String methodName) {
		List<MethodDeclaration> methodByName = getMethodByName(
				astRoot,
				methodName);
		if (methodByName.size() > 0) {
			MethodDeclaration methodDeclaration = methodByName.get(0);
			return methodDeclaration;
		}
		return null;
	}

	public static InjectionPoint getGuiceFieldDeclarationIfFieldDeclaration(
			ASTNode astNode, CompilationUnit astRoot) {
		if (!(astNode instanceof Name)) {
			return null;
		}
		Name name = (Name) astNode;
		final String variableName = name.getFullyQualifiedName();

		FieldDeclaration fieldDeclaration = ASTNodeUtils.getFieldDeclaration(name);
		if (fieldDeclaration != null) {
			InjectionPoint guiceFieldDeclaration = FieldDeclarationUtils.getTypeIfAnnotatedWithInject(
					fieldDeclaration,
					astRoot,
					variableName);

			return guiceFieldDeclaration;
		}
		return null;
	}

	public static IInjectionPoint getInjectionPoint(
			ASTNode astNode, CompilationUnit compilationUnit) {

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

		ProviderMethod guicyProviderMethodVar = getProviderMethod(
				name);
		
		return guicyProviderMethodVar;
	}

	public static ProviderMethod getProviderMethod(
			Name name) {
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
		return null;
	}

	@SuppressWarnings("unchecked")
	public static boolean isProviderMethod(ASTNode astNode) {
		if (astNode instanceof MethodDeclaration) {
			MethodDeclaration methodDeclaration = (MethodDeclaration) astNode;

			List<ASTNode> modifiers = methodDeclaration.modifiers();
			AnnotationList markerAnnotationList = ASTNodeUtils.getAnnotationList(modifiers);
			if (markerAnnotationList.containsProvidesAnnotation()) {
				return true;
			}
		}
		return false;
	}

}
