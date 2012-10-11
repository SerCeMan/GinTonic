package egap.utils;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;

import egap.guice.annotations.GuiceAnnotation;

/**
 * A {@link FieldDeclaration} which is annotated for injection.
 * 
 * @author tmajunke
 */
public class GuiceFieldDeclaration extends GuiceTypeWithAnnotation {

	private final FieldDeclaration fieldDeclaration;
	private final InjectionIsAttachedTo injectionIsAttachedTo;

	public GuiceFieldDeclaration(IProjectResource origin,
			ITypeBinding targetTypeBinding,
			GuiceAnnotation guiceAnnotation,
			String variableName,
			FieldDeclaration fieldDeclaration,
			InjectionIsAttachedTo injectionIsAttachedTo) {
		super(origin, targetTypeBinding, guiceAnnotation, variableName);
		this.fieldDeclaration = fieldDeclaration;
		this.injectionIsAttachedTo = injectionIsAttachedTo;
	}

	public FieldDeclaration getFieldDeclaration() {
		return fieldDeclaration;
	}

	/**
	 * Returns the the information from from which of the three possible places
	 * FIELD, SETTER or CONSTRUCTOR the injection information is attached to.
	 */
	public InjectionIsAttachedTo getInjectionIsAttachedTo() {
		return injectionIsAttachedTo;
	}

}
