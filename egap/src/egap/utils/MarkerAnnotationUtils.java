package egap.utils;


import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;


public class MarkerAnnotationUtils {

	public static boolean isOfType(
			MarkerAnnotation markerAnnotation, String typeFullyQualified) {
		ITypeBinding typeBinding = markerAnnotation.resolveTypeBinding();
		String qualifiedName = typeBinding.getQualifiedName();
		if (qualifiedName.equals(typeFullyQualified)) {
			return true;
		}
		return false;
	}

}
