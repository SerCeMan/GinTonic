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

	private boolean containsType(String annotationType) {
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

	public boolean containsInjectType() {
		return containsType(StringUtils.GUICE_ANNOTATION_INJECT);
	}

	public String getSingletonType() {
		if(containsType(StringUtils.GUICE_SCOPE_SINGLETON_NAME)){
			return StringUtils.GUICE_SCOPE_SINGLETON_NAME;
		}
		return null;
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
				if (annotationType.getQualifiedName().equals(
						StringUtils.GUICE_BINDING_ANNOTATION)) {
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
			if(ITypeBindingUtils.isKindOf(typeBinding, StringUtils.GUICE_NAMED)){
				IMemberValuePairBinding[] declaredMemberValuePairs = annotationBinding.getDeclaredMemberValuePairs();
				IMemberValuePairBinding pairBinding = declaredMemberValuePairs[0];
				String value = (String) pairBinding.getValue();
				return value;
			}
		}

		return null;
	}

	public boolean containsProvidesType() {
		return containsType(StringUtils.GUICE_PROVIDES);
	}

	public GuiceAnnotation getGuiceAnnotation() {
		String bindingAnnotation = getBindingAnnotation();
		
		if(bindingAnnotation != null){
			return new GuiceClassAnnotation(bindingAnnotation);
		}
		
		String namedAnnotationLiteralValue = getNamedAnnotationLiteralValue();
		if(namedAnnotationLiteralValue != null){
			return new GuiceNamedAnnotation(namedAnnotationLiteralValue);
		}
		return null;
	}

}
