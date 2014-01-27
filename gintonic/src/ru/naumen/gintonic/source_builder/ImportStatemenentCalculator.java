package ru.naumen.gintonic.source_builder;

import java.util.List;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

import ru.naumen.gintonic.utils.ListUtils;
import ru.naumen.gintonic.utils.Preconditions;



public class ImportStatemenentCalculator {

	private IType targetType;

	private List<SingleVariableDeclaration> variableDecl;

	public void setTargetType(IType targetType) {
		this.targetType = targetType;
	}

	public void setVariableDecl(List<SingleVariableDeclaration> variableDecl) {
		this.variableDecl = variableDecl;
	}

	/**
	 * Calculates the import statements based on a list of variable
	 * declarations. We do also filter anything from
	 * java.lang as the language spec tells us that we do not have to import
	 * these.
	 * 
	 * It is assumed that the import statements should be included
	 * in the given target type, which implies that we filter any references in
	 * the same package as the target class. The target class can also be null,
	 * which means we do not filter.
	 * 
	 * @return a list of type bindings which the target type has to import.
	 */
	public List<ITypeBinding> calculate() {

		List<ITypeBinding> importBindings = ListUtils.newArrayListWithCapacity(variableDecl.size());

		for (SingleVariableDeclaration singleVariableDeclaration : variableDecl) {
			Type type = singleVariableDeclaration.getType();
			Preconditions.checkNotNull(type);
			final ITypeBinding typeBinding = type.resolveBinding();
			Preconditions.checkNotNull(
					typeBinding,
					"No type binding available! Maybe you forgot to "
							+ "enable the binding-resolving in the ASTParser?");

			filterBindingsThatNeedToBeImported(typeBinding, importBindings);
		}

		List<ITypeBinding> filteredImportBindings = applyFilter(importBindings);

		return filteredImportBindings;
	}

	private void filterBindingsThatNeedToBeImported(
			final ITypeBinding typeBinding, List<ITypeBinding> importBindings) {

		/* e.g Dog[] */
		if (typeBinding.isArray()) {

			/* The component type for Dog[] is Dog */
			ITypeBinding componentTypeBinding = typeBinding.getComponentType();

			/* The component type may be another type => Recursion */
			filterBindingsThatNeedToBeImported(
					componentTypeBinding,
					importBindings);
		}

		/* e.g Map<String, Dog>. */
		else if (typeBinding.isParameterizedType()) {

			/* Type Declaration of Map<String, Dog> is Map */
			ITypeBinding typeDeclaration = typeBinding.getTypeDeclaration();
			importBindings.add(typeDeclaration);

			/*
			 * Recursion is needed to handle the nested case (e.g.,
			 * Vector<Vector<String>>).
			 */
			ITypeBinding[] typeArguments = typeBinding.getTypeArguments();
			for (ITypeBinding typeArgument : typeArguments) {
				filterBindingsThatNeedToBeImported(typeArgument, importBindings);
			}
		}

		else if (typeBinding.isPrimitive()) {
			/*
			 * Not important for import statement calculation as primitives
			 * don't require to be imported
			 */
		}

		else if (typeBinding.isWildcardType()) {
			ITypeBinding erasureBinding = typeBinding.getErasure();
			filterBindingsThatNeedToBeImported(erasureBinding, importBindings);
		}

		else if (typeBinding.isCapture()) {
			throw new RuntimeException("Capture type unsupported");
		}

		else {
			importBindings.add(typeBinding);
		}

	}

	protected List<ITypeBinding> applyFilter(List<ITypeBinding> typeBindings) {
		List<ITypeBinding> filteredTypeBindings = ListUtils.newArrayListWithCapacity(typeBindings.size());

		for (ITypeBinding typeBinding : typeBindings) {

			IPackageBinding packageBinding = typeBinding.getPackage();
			Preconditions.checkNotNull(packageBinding);

			String packageQualifiedName = packageBinding.getName();

			/* Ignore anything in java.lang */
			if (!packageQualifiedName.equals("java.lang")) {

				if(targetType != null){
					/*
					 * and any class which is in the same package as the target
					 * class
					 */
					IPackageFragment packageFragment = targetType.getPackageFragment();
					String packageOfTargetClass = packageFragment.getElementName();
					if (!packageQualifiedName.equals(packageOfTargetClass)) {
						filteredTypeBindings.add(typeBinding);
					}
				}else{
					filteredTypeBindings.add(typeBinding);
				}
				
			}
		}
		return filteredTypeBindings;
	}

}
