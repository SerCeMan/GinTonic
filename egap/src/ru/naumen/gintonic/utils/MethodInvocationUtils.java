package ru.naumen.gintonic.utils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class MethodInvocationUtils {

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

}
