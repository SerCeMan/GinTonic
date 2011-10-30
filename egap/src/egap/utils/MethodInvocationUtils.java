package egap.utils;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

public class MethodInvocationUtils {

	@SuppressWarnings("unchecked")
	public static String getSingleArgumentTypeAndTranslatePrimitiveToWrapper(MethodInvocation methodInvocation) {
		List<Expression> arguments = methodInvocation.arguments();
		Expression expression = arguments.get(0);
		return ExpressionUtils.getQualifiedTypeName(expression);
	}

	public static MethodInvocation resolveFirstMethodInvocation(
			MethodInvocation methodInvocation) {
		
		MethodInvocation currentMethodInvocation = methodInvocation;
		
		while(true){
			Expression expression = currentMethodInvocation.getExpression();
			if (expression == null) {
				return currentMethodInvocation;
			}
			if (expression instanceof MethodInvocation) {
				MethodInvocation previousMethodCall = (MethodInvocation) expression;
				currentMethodInvocation = previousMethodCall;
			} else {
				return currentMethodInvocation;
			}
		}
		
	}

	public static boolean isNameEqual(MethodInvocation methodInvocation,
			String nameToCompare) {
		SimpleName name = methodInvocation.getName();
		String fullyQualifiedName = name.getFullyQualifiedName();
		return fullyQualifiedName.equals(nameToCompare);
	}

}
