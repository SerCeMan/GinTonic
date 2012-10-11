package egap.utils;

import java.util.List;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;

import egap.guice.annotations.GuiceAnnotation;
import egap.guice.annotations.GuiceClassAnnotation;
import egap.guice.annotations.GuiceNamedAnnotation;

public class MarkerAnnotationList {

	private List<MarkerAnnotation> markerAnnotations;
	private final List<SingleMemberAnnotation> singleMemberAnnotations;

	MarkerAnnotationList(List<MarkerAnnotation> markerAnnotations,
			List<SingleMemberAnnotation> singleMemberAnnotations) {
		super();
		this.markerAnnotations = markerAnnotations;
		this.singleMemberAnnotations = singleMemberAnnotations;
	}

	/**
	 * Returns true if any of the annotations is of type
	 * {@link StringUtils#GUICE_ANNOTATION_ASSISTED}, otherwise false.
	 */
	public boolean containsAssistedAnnotation() {
		return containsAnnotation(StringUtils.GUICE_ANNOTATION_ASSISTED);
	}
	
	/**
	 * Returns true if any of the annotations is of type
	 * {@link StringUtils#GUICE_ANNOTATION_INJECT}, otherwise false.
	 */
	public boolean containsInjectType() {
		return containsAnnotation(StringUtils.GUICE_ANNOTATION_INJECT);
	}
	
	/**
	 * Returns true if any of the annotations is of type
	 * {@link StringUtils#GUICE_SCOPE_SINGLETON_NAME}, otherwise false.
	 */
	public boolean containsSingletonScopeAnnotation() {
		return containsAnnotation(StringUtils.GUICE_SCOPE_SINGLETON_NAME);
	}
	
	/**
	 * Returns true if any of the annotations is of type
	 * {@link StringUtils#GUICE_PROVIDES}, otherwise false.
	 */
	public boolean containsProvidesAnnotation() {
		return containsAnnotation(StringUtils.GUICE_PROVIDES);
	}
	
	public GuiceAnnotation getGuiceAnnotation() {
		String bindingAnnotation = getBindingAnnotation();

		if (bindingAnnotation != null) {
			return new GuiceClassAnnotation(bindingAnnotation);
		}

		String namedAnnotationLiteralValue = getNamedAnnotationLiteralValue();
		if (namedAnnotationLiteralValue != null) {
			return new GuiceNamedAnnotation(namedAnnotationLiteralValue);
		}
		return null;
	}
	
	private boolean containsAnnotation(String annotationType) {
		for (MarkerAnnotation markerAnnotation : markerAnnotations) {
			boolean ofType = MarkerAnnotationUtils.isOfType(
					markerAnnotation,
					annotationType);
			if (ofType) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the name of the binding annotation or null otherwise.
	 */
	private String getBindingAnnotation() {
		for (MarkerAnnotation markerAnnotation : markerAnnotations) {
			ITypeBinding typeBinding = markerAnnotation.resolveTypeBinding();
			IAnnotationBinding[] annotations = typeBinding.getAnnotations();
			for (IAnnotationBinding annotation : annotations) {
				ITypeBinding annotationType = annotation.getAnnotationType();
				if (ITypeBindingUtils.isGuiceBindingAnnotation(annotationType)) {
					return typeBinding.getQualifiedName();
				}
			}
		}

		return null;
	}

	private String getNamedAnnotationLiteralValue() {
		for (SingleMemberAnnotation singleMemberAnnotation : singleMemberAnnotations) {
			IAnnotationBinding annotationBinding = singleMemberAnnotation.resolveAnnotationBinding();
			ITypeBinding typeBinding = singleMemberAnnotation.resolveTypeBinding();
			if (ITypeBindingUtils.isGuiceNamedType(typeBinding)) {
				IMemberValuePairBinding[] declaredMemberValuePairs = annotationBinding.getDeclaredMemberValuePairs();
				IMemberValuePairBinding pairBinding = declaredMemberValuePairs[0];
				String value = (String) pairBinding.getValue();
				return value;
			}
		}

		return null;
	}

}
