package ru.naumen.gintonic.quickfix.assisted;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

import ru.naumen.gintonic.context.quickfix.AbstractGinTonicQuickFix;
import ru.naumen.gintonic.guice.GuiceIndex;
import ru.naumen.gintonic.guice.GuiceModule;
import ru.naumen.gintonic.guice.statements.BindingDefinition;
import ru.naumen.gintonic.utils.ASTNodeUtils;
import ru.naumen.gintonic.utils.ICompilationUnitUtils;
import ru.naumen.gintonic.utils.MethodDeclarationUtils;


/**
 * @author tmajunke
 */
public class QuickFixAssistedInject extends AbstractGinTonicQuickFix {

	@Override
	public void addProposals(IInvocationContext context,
			List<IJavaCompletionProposal> proposals) throws CoreException {

		ASTNode coveringNode = context.getCoveringNode();
		TypeDeclaration typeDeclaration = ASTNodeUtils.getTypeDeclaration(coveringNode);
		if(typeDeclaration == null){
			return;
		}

		MethodDeclaration constructorWithAssistedAnnotation = MethodDeclarationUtils.getConstructorAnnotatedWithAssisted(typeDeclaration);
		if (constructorWithAssistedAnnotation == null) {
			return;
		}
		ICompilationUnit compilationUnit = context.getCompilationUnit();
		IType modelType = compilationUnit.findPrimaryType();
		IPackageFragment modelPackage = modelType.getPackageFragment();
		/*
		 * We derive the name of the factory from the CompilationUnit from (eg, person => person Factory)
		 */
		String modelTypeName = modelType.getElementName();
		String factoryTypeName = modelTypeName + "Factory";

		ICompilationUnit factory = modelPackage.getCompilationUnit(factoryTypeName
				+ ICompilationUnitUtils.JAVA_EXTENSION);

		if (factory.exists()) {
			GuiceIndex guiceIndex = GuiceIndex.get();

			String fullyQualifiedName = modelType.getFullyQualifiedName();
			BindingDefinition bindStatement = guiceIndex.getAssistedBindingDefinitionsByModelType(fullyQualifiedName);

			if (bindStatement == null) {
				ITypeBinding typeBinding = ((Name) coveringNode).resolveTypeBinding();
				IPackageBinding currentPackage = typeBinding.getPackage();
				List<GuiceModule> guiceModules = guiceIndex.getGuiceModulesInAndBelowPackage(currentPackage);

				IType factoryType = factory.findPrimaryType();
				for (GuiceModule guiceModule : guiceModules) {
					ProposalCreateBindingForAssistedFactory proposal = new ProposalCreateBindingForAssistedFactory(
							guiceModule,
							factoryType,
							modelType);
					proposals.add(proposal);
				}
			}
		} else {
			ProposalCreateAssistedFactory proposal = new ProposalCreateAssistedFactory(
					constructorWithAssistedAnnotation,
					modelPackage,
					factoryTypeName,
					modelType);
			proposals.add(proposal);
		}

	}

}
