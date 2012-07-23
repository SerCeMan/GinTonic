package egap.utils;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

import egap.guice.ProjectResource;
import egap.guice.annotations.GuiceAnnotation;

public class FieldDeclarationUtils {

	/**
	 * Checks if any of the markers of the given {@link FieldDeclaration} is an
	 * annotation of the given type.
	 * 
	 * @param fieldDeclaration the given {@link FieldDeclaration}
	 * @param markerAnnotation the type to check for (fully qualified name)
	 * @return true if any of the markers of the given {@link FieldDeclaration}
	 *         are an annotation of the given type, otherwise false.
	 */
	public static boolean isAnnotatedWithMarkerAnnotation(
			FieldDeclaration fieldDeclaration, String markerAnnotation) {
		@SuppressWarnings("unchecked")
		List<ASTNode> modifiers = fieldDeclaration.modifiers();

		for (ASTNode modifier : modifiers) {
			boolean foundAnnotation = ASTNodeUtils.isMarkerAnnotationAnnotatedWith(
					modifier,
					markerAnnotation);
			if (foundAnnotation) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns true if the given {@link FieldDeclaration}s type is a
	 * {@link ParameterizedType} and the type's qualified name is equal to the
	 * base type one (given as qualified name).
	 * 
	 * @param fieldDeclaration the {@link FieldDeclaration}
	 * @param baseType the qualified name of the base type.
	 * @return true if it is, otherwise false.
	 */
	public static boolean isParameterizedTypeOfBaseType(
			FieldDeclaration fieldDeclaration, String baseType) {
		Type fieldType = fieldDeclaration.getType();
		if (!fieldType.isParameterizedType()) {
			return false;
		}
		ITypeBinding typeBinding = fieldType.resolveBinding();
		if (!typeBinding.isParameterizedType()) {
			return false;
		}
		ITypeBinding typeDeclaration = typeBinding.getTypeDeclaration();
		boolean kindOf = ITypeBindingUtils.isKindOf(typeDeclaration, baseType);
		return kindOf;
	}

	@SuppressWarnings("unchecked")
	public static GuiceFieldDeclaration getTypeIfAnnotatedWithInject(ProjectResource origin, FieldDeclaration fieldDeclaration,
			CompilationUnit compilationUnit, String fieldName) {

		List<ASTNode> modifiers = fieldDeclaration.modifiers();
		MarkerAnnotationList annotationList = ASTNodeUtils.getMarkerAnnotationList(modifiers);

		if (annotationList.containsInjectType()) {
			Type type = fieldDeclaration.getType();
			GuiceAnnotation guiceAnnotation = annotationList.getGuiceAnnotation();
			return new GuiceFieldDeclaration(origin, type.resolveBinding(), guiceAnnotation, fieldName, fieldDeclaration);
		}

		/*
		 * Check the @Inject constructor if we can find a parameter with the
		 * same name as the selected field.
		 */
		MethodDeclaration constructor = ASTNodeUtils.getConstructorAnnotatedWithInject(compilationUnit);
		if (constructor != null) {
			SingleVariableDeclaration variableDeclaration = MethodDeclarationUtils.getVariableDeclarationsByName(
					constructor,
					fieldName);
			if (variableDeclaration != null) {
				annotationList = ASTNodeUtils.getMarkerAnnotationList(variableDeclaration.modifiers());
				Type type = variableDeclaration.getType();
				GuiceAnnotation guiceAnnotation = annotationList.getGuiceAnnotation();
				return new GuiceFieldDeclaration(origin, type.resolveBinding(), guiceAnnotation, fieldName, fieldDeclaration);
			}
		}
		
		/*
		 * Check if a guicified setter method exists. If it does 
		 */
		String setterMethodName = "set" + StringUtils.capitalize(fieldName);
		
		MethodDeclaration setter = ASTNodeUtils.getMethodByNameExpectSingleMethod(
				compilationUnit,
				setterMethodName);
		if (setter != null) {
			annotationList = ASTNodeUtils.getMarkerAnnotationList(setter.modifiers());
			if (annotationList.containsInjectType()) {
				Type type = fieldDeclaration.getType();
				GuiceAnnotation guiceAnnotation = annotationList.getGuiceAnnotation();
				return new GuiceFieldDeclaration(origin, type.resolveBinding(), guiceAnnotation, fieldName, fieldDeclaration);
			}
		}
		return null;
	}

}
