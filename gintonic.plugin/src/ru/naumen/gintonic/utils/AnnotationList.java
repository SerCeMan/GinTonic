package ru.naumen.gintonic.utils;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import ru.naumen.gintonic.guice.GuiceConstants;
import ru.naumen.gintonic.guice.annotations.GuiceClassAnnotation;
import ru.naumen.gintonic.guice.annotations.GuiceNamedAnnotation;
import ru.naumen.gintonic.guice.annotations.IGuiceAnnotation;


public class AnnotationList {

	private final List<Annotation> annotations;

	public AnnotationList(List<Annotation> annotations) {
		super();
		this.annotations = annotations;
	}

	/**
	 * Returns true if any of the annotations is of type
	 * {@link GuiceConstants#ANNOTATION_ASSISTED}, otherwise false.
	 */
	public boolean containsAssistedAnnotation() {
		return containsAnnotation(GuiceConstants.ANNOTATION_ASSISTED);
	}

	/**
	 * Returns true if any of the annotations is of type
	 * {@link GuiceConstants#ANNOTATION_INJECTS}, otherwise false.
	 */
	public boolean containsInjectType() {
	    return containsAnyAnnotation(GuiceConstants.ANNOTATIONS_INJECT);
	}

	/**
	 * Returns true if any of the annotations is of type
	 * {@link GuiceConstants#SINGLETON_SCOPE}, otherwise false.
	 */
	public boolean containsSingletonScopeAnnotation() {
	    return containsAnyAnnotation(GuiceConstants.SINGLETON_ANNOTATIONS);
	}

	/**
	 * Returns true if any of the annotations is of type
	 * {@link GuiceConstants#PROVIDES}, otherwise false.
	 */
	public boolean containsProvidesAnnotation() {
		return containsAnnotation(GuiceConstants.PROVIDES);
	}

	public IGuiceAnnotation getGuiceAnnotation() {
		String namedAnnotationLiteralValue = getNamedAnnotationLiteralValue();
		if (namedAnnotationLiteralValue != null) {
			return new GuiceNamedAnnotation(namedAnnotationLiteralValue);
		}

		String bindingAnnotation = getBindingAnnotation();
		if (bindingAnnotation != null) {
			return new GuiceClassAnnotation(bindingAnnotation);
		}

		return null;
	}
	
	private boolean containsAnyAnnotation(Collection<String> annotationTypes) {
	    for(String annotationType : annotationTypes) {
	        if(containsAnnotation(annotationType)) {
	            return true;
	        }
	    }
	    return false;
	}

	private boolean containsAnnotation(String annotationType) {
		for (Annotation annotation : annotations) {
			boolean ofType = isOfType(
					annotation,
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
		for (Annotation annotation : annotations) {
			ITypeBinding typeBinding = annotation.resolveTypeBinding();
			IAnnotationBinding[] annotations = typeBinding.getAnnotations();
			for (IAnnotationBinding annotationBinding : annotations) {
				ITypeBinding annotationType = annotationBinding.getAnnotationType();
				if (ITypeBindingUtils.isGuiceBindingAnnotation(annotationType)) {
					return typeBinding.getQualifiedName();
				}
			}
		}

		return null;
	}

	private String getNamedAnnotationLiteralValue() {
		for (Annotation annotation : annotations) {
			IAnnotationBinding annotationBinding = annotation.resolveAnnotationBinding();
			ITypeBinding typeBinding = annotation.resolveTypeBinding();
			if (ITypeBindingUtils.isGuiceNamedType(typeBinding)) {
				IMemberValuePairBinding[] declaredMemberValuePairs = annotationBinding.getDeclaredMemberValuePairs();
				IMemberValuePairBinding pairBinding = declaredMemberValuePairs[0];
				String value = (String) pairBinding.getValue();
				return value;
			}
		}

		return null;
	}

	private boolean isOfType(
			Annotation markerAnnotation, String typeFullyQualified) {
		ITypeBinding typeBinding = markerAnnotation.resolveTypeBinding();
		String qualifiedName = typeBinding.getQualifiedName();
		if (qualifiedName.equals(typeFullyQualified)) {
			return true;
		}
		return false;
	}

}
