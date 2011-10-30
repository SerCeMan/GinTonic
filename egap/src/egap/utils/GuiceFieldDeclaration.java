package egap.utils;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;

import egap.guice.annotations.GuiceAnnotation;

public class GuiceFieldDeclaration extends GuiceTypeWithAnnotation {

	private final FieldDeclaration fieldDeclaration;

	public GuiceFieldDeclaration(IProjectResource origin,
			ITypeBinding targetTypeBinding,
			GuiceAnnotation guiceAnnotation,
			String variableName,
			FieldDeclaration fieldDeclaration) {
		super(origin, targetTypeBinding, guiceAnnotation, variableName);
		this.fieldDeclaration = fieldDeclaration;
	}

	public FieldDeclaration getFieldDeclaration() {
		return fieldDeclaration;
	}

}
