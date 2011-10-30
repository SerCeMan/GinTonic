package egap.utils;

import org.eclipse.jdt.core.dom.ITypeBinding;

import egap.guice.annotations.GuiceAnnotation;

public class GuiceProviderMethodVar extends GuiceTypeWithAnnotation {

	public GuiceProviderMethodVar(IProjectResource origin,
			ITypeBinding targetTypeBinding,
			GuiceAnnotation guiceAnnotation,
			String variableName) {
		super(origin, targetTypeBinding, guiceAnnotation, variableName);
	}

}
