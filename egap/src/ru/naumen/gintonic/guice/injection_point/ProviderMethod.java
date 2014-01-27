package ru.naumen.gintonic.guice.injection_point;

import org.eclipse.jdt.core.dom.ITypeBinding;

import ru.naumen.gintonic.guice.annotations.IGuiceAnnotation;

public class ProviderMethod implements IInjectionPoint {

    private final ITypeBinding targetTypeBinding;
    private final IGuiceAnnotation guiceAnnotation;
    private final String identifier;

    public ProviderMethod(ITypeBinding targetTypeBinding, IGuiceAnnotation guiceAnnotation, String identifier) {
        this.targetTypeBinding = targetTypeBinding;
        this.guiceAnnotation = guiceAnnotation;
        this.identifier = identifier;
    }

    @Override
    public ITypeBinding getTargetTypeBinding() {
        return targetTypeBinding;
    }

    @Override
    public IGuiceAnnotation getGuiceAnnotation() {
        return guiceAnnotation;
    }

    /**
     * Returns the name of the selected identifier.
     */
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return "ProviderMethod [targetTypeBinding=" + targetTypeBinding + ", guiceAnnotation=" + guiceAnnotation
                + ", identifier=" + identifier + "]";
    }
}
