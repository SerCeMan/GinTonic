package egap.utils;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StringLiteral;

import egap.guice.annotations.GuiceAnnotation;
import egap.guice.annotations.GuiceClassAnnotation;
import egap.guice.annotations.GuiceNamedAnnotation;

public class ExpressionUtils {

	public static String getQualifiedTypeName(Expression expression) {
		ITypeBinding typeBinding = expression.resolveTypeBinding();
		typeBinding = ITypeBindingUtils.removeSurroundingTypeLiteral(typeBinding);
		typeBinding = ITypeBindingUtils.removeSurroundingClassType(typeBinding);
		String qualifiedName = typeBinding.getQualifiedName();
		qualifiedName = StringUtils.translatePrimitiveToWrapper(qualifiedName);
		return qualifiedName;
	}

	/**
	 * Checks if the expression is a Names.named("jack") and returns the Literal
	 * value (in this case "jack").
	 */
	@SuppressWarnings("unchecked")
	private static String getNamedLiteralValue(Expression expression) {
		if (expression instanceof MethodInvocation) {
			MethodInvocation mInvocation = (MethodInvocation) expression;
			String methodName = mInvocation.getName().getFullyQualifiedName();

			/* annotatedWith(Names.named("jack")) */
			if (methodName.equals("named")) {
				List<Expression> arguments2 = mInvocation.arguments();
				StringLiteral stringLiteral = (StringLiteral) arguments2.get(0);
				String namedAnnotation = stringLiteral.getLiteralValue();
				return namedAnnotation;
			}
		}

		return null;
	}

	public static GuiceAnnotation resolveGuiceAnnotation(
			Expression expression) {
		
		String literalValue = getNamedLiteralValue(expression);
		if(literalValue != null){
			return new GuiceNamedAnnotation(literalValue);
		}

		String qualifiedTypeName = getQualifiedTypeName(expression);
		if(qualifiedTypeName != null){
			return new GuiceClassAnnotation(qualifiedTypeName);
		}
		
		
		return null;
	}

}
