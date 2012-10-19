package egap.utils;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;

import egap.guice.annotations.GuiceAnnotation;

/**
 * An injection point is a {@link FieldDeclaration} which is annotated for
 * injection. The injection information can be attached to the field itself, the
 * setter or the constructor (see {@link #getInjectionIsAttachedTo()}).
 * 
 * <h1>Example:</h1>
 * 
 * <pre>
 * &#64Inject
 * private Person person;
 * </pre>
 * 
 * @author tmajunke
 */
public class InjectionPoint implements IAnnotatedInjectionPoint{

	private final FieldDeclaration fieldDeclaration;
	private final InjectionIsAttachedTo injectionIsAttachedTo;
	private final ITypeBinding targetTypeBinding;
	private final GuiceAnnotation guiceAnnotation;
	private final String variableName;

	public InjectionPoint(ITypeBinding targetTypeBinding,
			GuiceAnnotation guiceAnnotation,
			String variableName,
			FieldDeclaration fieldDeclaration,
			InjectionIsAttachedTo injectionIsAttachedTo) {
		this.targetTypeBinding = targetTypeBinding;
		this.guiceAnnotation = guiceAnnotation;
		this.variableName = variableName;
		this.fieldDeclaration = fieldDeclaration;
		this.injectionIsAttachedTo = injectionIsAttachedTo;
	}

	/**
	 * Returns the {@link FieldDeclaration}.
	 */
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
	
	@Override
	public ITypeBinding getTargetTypeBinding() {
		return targetTypeBinding;
	}
	
	@Override
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
