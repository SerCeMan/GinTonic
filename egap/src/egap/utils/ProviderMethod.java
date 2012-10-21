package egap.utils;

import org.eclipse.jdt.core.dom.ITypeBinding;

import egap.guice.annotations.GuiceAnnotation;

public class ProviderMethod implements IAnnotatedInjectionPoint{

	private final ITypeBinding targetTypeBinding;
	private final GuiceAnnotation guiceAnnotation;
	private final String identifier;

	public ProviderMethod(ITypeBinding targetTypeBinding,
			GuiceAnnotation guiceAnnotation,
			String identifier) {
		this.targetTypeBinding = targetTypeBinding;
		this.guiceAnnotation = guiceAnnotation;
		this.identifier = identifier;

	}

	@Override
	public ITypeBinding getTargetTypeBinding() {
		return targetTypeBinding;
	}

	@Override
	public GuiceAnnotation getGuiceAnnotation() {
		return guiceAnnotation;
	}

	/**
	 * Returns the name of the selected identifier.
	 */
	public String getIdentifier() {
		return identifier;
	}

}
