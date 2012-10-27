package egap.utils;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;

public class ITypeBindingUtils {

	/**
	 * Examines the given type binding if it is an instance of the type.
	 * Processes parent class- and interface check.
	 * 
	 * @param typeBinding the type binding
	 * @param fullyQualifiedTypeName the fully qualified name to search for. May
	 *            be a class or an interface.
	 * @return true if the given type binding is an instance of the given type,
	 *         otherwise false.
	 */
	private static boolean isKindOf(ITypeBinding typeBinding,
			String fullyQualifiedTypeName) {
		ITypeBinding kindOf = getKindOf(typeBinding, fullyQualifiedTypeName);
		return kindOf != null;
	}

	public static boolean isGuiceAbstractModuleType(ITypeBinding typeBinding) {
		return isKindOf(typeBinding, StringUtils.GUICE_ABSTRACT_MODULE);
	}

	public static boolean isGuiceProviderType(ITypeBinding typeBinding) {
		return isKindOf(typeBinding, StringUtils.GUICE_PROVIDER);
	}

	public static boolean isGuiceNamedType(ITypeBinding typeBinding) {
		return isKindOf(typeBinding, StringUtils.GUICE_NAMED);
	}
	
	public static boolean isGuiceBindingAnnotation(ITypeBinding typeBinding) {
		String qualifiedName = typeBinding.getQualifiedName();
		return qualifiedName.equals(
				StringUtils.GUICE_BINDING_ANNOTATION);
	}

	public static boolean isConcreteType(ITypeBinding typeBinding) {
		if (typeBinding.isInterface()) {
			return false;
		}

		int declaredModifiers = typeBinding.getDeclaredModifiers();
		boolean isAbstract = Modifier.isAbstract(declaredModifiers);
		if (isAbstract) {
			return false;
		}

		boolean isClass = typeBinding.isClass();

		return isClass;
	}

	/**
	 * Examines the given type binding if it is an instance of the type and if
	 * it is then the {@link ITypeBinding} is returned. Processes the super
	 * classes and the interfaces to check for type equality.
	 * 
	 * @param typeBinding the type binding
	 * @param fullyQualifiedTypeName the fully qualified name to search for. May
	 *            be a class or an interface.
	 * @return true the binding for the qualified name or null if it is not of
	 *         this type.
	 */
	public static ITypeBinding getKindOf(ITypeBinding typeBinding,
			String fullyQualifiedTypeName) {

		if (typeBinding == null) {
			return null;
		}
		
		ITypeBinding typeBindingToGetKindOf = typeBinding;
		
		if (typeBindingToGetKindOf.isParameterizedType()) {
			typeBindingToGetKindOf = typeBindingToGetKindOf.getErasure();
		}

		String qualifiedName = typeBindingToGetKindOf.getQualifiedName();
		if (fullyQualifiedTypeName.equals(qualifiedName)) {
			return typeBindingToGetKindOf;
		}

		ITypeBinding[] interfaces = typeBindingToGetKindOf.getInterfaces();
		for (int i = 0; i < interfaces.length; i++) {
			ITypeBinding interfaceBinding = getKindOf(
					interfaces[i],
					fullyQualifiedTypeName);

			if (interfaceBinding != null) {
				return interfaceBinding;
			}
		}

		ITypeBinding superClass = typeBindingToGetKindOf.getSuperclass();
		ITypeBinding superclassTypeBinding = getKindOf(
				superClass,
				fullyQualifiedTypeName);
		return superclassTypeBinding;
	}

	public static ITypeBinding removeSurroundingClassType(
			ITypeBinding typeBinding) {
		return removeSurroundingType(typeBinding, StringUtils.CLASS_TYPE);
	}

	public static ITypeBinding removeSurroundingTypeLiteral(
			ITypeBinding typeBinding) {
		return removeSurroundingType(
				typeBinding,
				StringUtils.GUICE_TYPE_LITERAL);
	}

	/*
	 * Check if the given type is a Provider(e.g Provider<Date>) type. If it is
	 * then we remove the Provider and return the wrapped part (e.g for
	 * "Provider<Date>" the wrapped part is "Date").
	 */
	public static ITypeBinding removeSurroundingProvider(
			ITypeBinding typeBinding) {
		return removeSurroundingType(typeBinding, StringUtils.GUICE_PROVIDER);
	}

	private static ITypeBinding removeSurroundingType(ITypeBinding typeBinding,
			String surroundingType) {

		ITypeBinding typeBindingToUnwrap = typeBinding;

		if (typeBinding.isAnonymous()) {
			typeBindingToUnwrap = typeBinding.getSuperclass();
		}

		ITypeBinding typeDeclaration = typeBindingToUnwrap.getTypeDeclaration();
		String qualifiedName = typeDeclaration.getQualifiedName();

		boolean isClassType = qualifiedName.equals(surroundingType);
		if (isClassType) {
			ITypeBinding[] typeArguments = typeBindingToUnwrap.getTypeArguments();
			return typeArguments[0];
		}

		return typeBinding;
	}

}
