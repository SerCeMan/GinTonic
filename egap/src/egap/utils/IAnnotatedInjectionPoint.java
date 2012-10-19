package egap.utils;

import egap.guice.annotations.GuiceAnnotation;

public interface IAnnotatedInjectionPoint extends IInjectionPoint{

	/**
	 * Returns the annotation or null if it is not annotated.
	 */
	public abstract GuiceAnnotation getGuiceAnnotation();

}