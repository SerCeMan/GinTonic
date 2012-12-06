package de.jaculon.egap.guice.injection_point;

import org.eclipse.jdt.core.dom.ITypeBinding;

import de.jaculon.egap.guice.annotations.GuiceAnnotation;

/**
 * @author tmajunke
 */
public interface IInjectionPoint {

	/**
	 * Returns the {@link ITypeBinding} of the injection point. Is never null.
	 */
	ITypeBinding getTargetTypeBinding();

	/**
	 * Returns the annotation or null if it is not annotated.
	 */
	GuiceAnnotation getGuiceAnnotation();

}