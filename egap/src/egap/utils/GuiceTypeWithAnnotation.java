package egap.utils;

import org.eclipse.jdt.core.dom.ITypeBinding;

import egap.guice.annotations.GuiceAnnotation;

public class GuiceTypeWithAnnotation extends GuiceTypeInfo {
	
	private final GuiceAnnotation guiceAnnotation;
	private final String variableName;

	public GuiceTypeWithAnnotation(
			IProjectResource origin,
			ITypeBinding targetTypeBinding,
			GuiceAnnotation guiceAnnotation,
			String variableName) {
		super(origin, targetTypeBinding);
		this.guiceAnnotation = guiceAnnotation;
		this.variableName = variableName;
	}

	public GuiceAnnotation getGuiceAnnotation() {
		return guiceAnnotation;
	}

	/**
	 * Returns the unqualified variable name.
	 */
	public String getVariableName() {
		return variableName;
	}

}
