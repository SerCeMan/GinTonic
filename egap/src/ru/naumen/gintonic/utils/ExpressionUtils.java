package ru.naumen.gintonic.utils;

import java.util.List;

import org.eclipse.jdt.core.dom.*;

import ru.naumen.gintonic.guice.annotations.GuiceClassAnnotation;
import ru.naumen.gintonic.guice.annotations.GuiceNamedAnnotation;
import ru.naumen.gintonic.guice.annotations.IGuiceAnnotation;

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
                Expression argument = arguments2.get(0);
                String namedAnnotation = resolveStringExpression(argument);
                if (namedAnnotation == null) {
                    throw new IllegalArgumentException("Resolved namedAnnotation should not be null");
                }
                return namedAnnotation;
            }
        }

        return null;
    }
    
    private static String resolveStringExpression(Expression argument) {
        String namedAnnotation = null;
        if (argument instanceof StringLiteral) {
            StringLiteral stringLiteral = (StringLiteral) argument;
            namedAnnotation = stringLiteral.getLiteralValue();
        } else if (argument instanceof SimpleName) {
            namedAnnotation = resolveSimpleName((SimpleName) argument);
        } else if (argument instanceof QualifiedName) {
            namedAnnotation = resolveSimpleName(((QualifiedName) argument).getName());
        }
        return namedAnnotation;
    }

    private static String resolveSimpleName(SimpleName argument) {
        IVariableBinding variableBinding = ASTNodeUtils.getVariableBinding(argument);
        Object constantValue = variableBinding.getConstantValue();
        return constantValue == null ? null : constantValue.toString();
    }
    
    
    public static IGuiceAnnotation resolveGuiceAnnotation(Expression expression) {

        String literalValue = getNamedLiteralValue(expression);
        if (literalValue != null) {
            return new GuiceNamedAnnotation(literalValue);
        }

        String qualifiedTypeName = getQualifiedTypeName(expression);
        if (qualifiedTypeName != null) {
            return new GuiceClassAnnotation(qualifiedTypeName);
        }

        return null;
    }

}
