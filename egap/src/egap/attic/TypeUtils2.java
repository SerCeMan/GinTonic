package egap.attic;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;

import egap.utils.ITypeBindingUtils;

@Deprecated
public class TypeUtils2 {

	public static boolean isKindOf(Type type,
			String typeFullyQualifiedName) {
		ITypeBinding resolveBinding = type.resolveBinding();
		return ITypeBindingUtils.isKindOf(resolveBinding, typeFullyQualifiedName);
	}

}
