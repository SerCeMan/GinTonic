package egap.utils;

import org.eclipse.jdt.core.dom.ITypeBinding;

import egap.guice.annotations.GuiceAnnotation;

public class ProviderMethod implements IAnnotatedInjectionPoint{

	private final ITypeBinding targetTypeBinding;
	private final GuiceAnnotation guiceAnnotation;
	private final String variableName;

	public ProviderMethod(ITypeBinding targetTypeBinding,
			GuiceAnnotation guiceAnnotation,
			String variableName) {
		this.targetTypeBinding = targetTypeBinding;
		this.guiceAnnotation = guiceAnnotation;
		this.variableName = variableName;

	}

	@Override
	public ITypeBinding getTargetTypeBinding() {
		return targetTypeBinding;
	}

	@Override
	public GuiceAnnotation getGuiceAnnotation() {
		return guiceAnnotation;
	}

	public String getVariableName() {
		return variableName;
	}

}
