package egap.utils;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

import egap.guice.ProjectResource;
import egap.guice.annotations.GuiceAnnotation;

public class FieldDeclarationUtils {

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
