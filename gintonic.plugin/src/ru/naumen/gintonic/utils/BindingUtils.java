package ru.naumen.gintonic.utils;

import ru.naumen.gintonic.guice.injection.IInjectionPoint;
import ru.naumen.gintonic.guice.statements.BindingDefinition;
import ru.naumen.gintonic.guice.statements.LinkedBindingStatement;

/**
 * @author Sergey Tselovalnikov
 * @since Jan 27, 2014
 */
public final class BindingUtils {
    
    
    private BindingUtils() {
    }

    public static String extractTypeName(IInjectionPoint injectionPoint, BindingDefinition bindingDefinition) {
        String typeName = null;
        if (bindingDefinition instanceof LinkedBindingStatement) {
            LinkedBindingStatement binding = (LinkedBindingStatement) bindingDefinition;
            typeName = binding.getImplType();
        } else if (bindingDefinition != null) {
            typeName = bindingDefinition.getBoundType();
        }
        if (typeName == null && injectionPoint.getTargetTypeBinding() != null) {
            // Maybe class has not bindings, no annotation, but it injected and DI know about it
            typeName = injectionPoint.getTargetTypeBinding().getQualifiedName();
        }
        return typeName;
    }
}
