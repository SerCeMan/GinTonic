package egap.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

import egap.guice.ProjectResource;
import egap.guice.annotations.GuiceAnnotation;

public class FieldDeclarationUtils {

	@SuppressWarnings("unchecked")
	public static GuiceFieldDeclaration getTypeIfAnnotatedWithInject(
			ProjectResource origin, FieldDeclaration fieldDeclaration,
			CompilationUnit compilationUnit, String fieldName) {

		List<ASTNode> modifiers = fieldDeclaration.modifiers();

		MarkerAnnotationList annotationList = ASTNodeUtils.getMarkerAnnotationList(modifiers);

		if (annotationList.containsInjectType()) {
			Type type = fieldDeclaration.getType();
			GuiceAnnotation guiceAnnotation = annotationList.getGuiceAnnotation();
			return new GuiceFieldDeclaration(
					origin,
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
		MethodDeclaration constructor = ASTNodeUtils.getConstructorAnnotatedWithInject(compilationUnit);
		if (constructor != null) {
			SingleVariableDeclaration variableDeclaration = MethodDeclarationUtils.getVariableDeclarationsByName(
					constructor,
					fieldName);
			if (variableDeclaration != null) {
				annotationList = ASTNodeUtils.getMarkerAnnotationList(variableDeclaration.modifiers());
				Type type = variableDeclaration.getType();
				GuiceAnnotation guiceAnnotation = annotationList.getGuiceAnnotation();
				return new GuiceFieldDeclaration(
						origin,
						type.resolveBinding(),
						guiceAnnotation,
						fieldName,
						fieldDeclaration,
						InjectionIsAttachedTo.CONSTRUCTOR);
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
				return new GuiceFieldDeclaration(
						origin,
						type.resolveBinding(),
						guiceAnnotation,
						fieldName,
						fieldDeclaration,
						InjectionIsAttachedTo.SETTER);
			}
		}
		return null;
	}

	/**
	 * Returns true if the field declaration is static.
	 * 
	 * @param fieldDeclaration the field declaration
	 */
	public static boolean isStatic(FieldDeclaration fieldDeclaration) {
		@SuppressWarnings("unchecked")
		List<ASTNode> modifiers = fieldDeclaration.modifiers();
		for (ASTNode astNode : modifiers) {
			if (astNode instanceof Modifier) {
				Modifier modifier = (Modifier) astNode;
				if (modifier.isStatic()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Deletes all annotations from the given field declaration.
	 * 
	 * @param fieldDeclaration the field declaration
	 */
	public static void removeAnnotations(FieldDeclaration fieldDeclaration) {
		@SuppressWarnings("unchecked")
		List<ASTNode> modifiers = fieldDeclaration.modifiers();
		List<ASTNode> annotationsToDelete = new ArrayList<ASTNode>(
				modifiers.size());
		for (ASTNode astNode : modifiers) {
			if (astNode instanceof Annotation) {
				annotationsToDelete.add(astNode);

				/*
				 * astNode.delete(); doesnt work here. We have to collect the
				 * annotations first and then delete them!
				 */
			}
		}
		for (ASTNode astNode : annotationsToDelete) {
			astNode.delete();
		}
	}

	/**
	 * Adds the annotation to the field declaration.
	 */
	public static void addMarkerAnnotation(FieldDeclaration fieldDeclaration,
			String annotationUnqualifiedName) {
		AST ast = fieldDeclaration.getAST();
		MarkerAnnotation annotation = ast.newMarkerAnnotation();
		SimpleName name = ast.newSimpleName(annotationUnqualifiedName);
		annotation.setTypeName(name);

		@SuppressWarnings("unchecked")
		List<ASTNode> modifiers = fieldDeclaration.modifiers();
		modifiers.add(0, annotation);
	}

}
