package egap.quickfix.binding_creation;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;


import egap.guice.GuiceIndex;
import egap.guice.GuiceModule;
import egap.guice.statements.GuiceStatement;
import egap.quickfix.AbstractEgapQuickFix;
import egap.utils.ASTNodeUtils;
import egap.utils.InjectionPoint;
import egap.utils.ITypeBindingUtils;
import egap.utils.SetUtils;

/**
 * @author tmajunke
 */
public class QuickFixProviderMethodCreation extends AbstractEgapQuickFix {

	@Override
	public void addProposals(IInvocationContext context,
			List<IJavaCompletionProposal> proposals) throws CoreException {
		CompilationUnit astRoot = context.getASTRoot();
		
		InjectionPoint guiceFieldDecl = ASTNodeUtils.getGuiceFieldDeclarationIfFieldDeclaration(
				context.getCoveringNode(),
				astRoot);

		if (guiceFieldDecl != null) {
			ITypeBinding typeBindingWithoutProvider = ITypeBindingUtils.removeSurroundingProvider(guiceFieldDecl.getTargetTypeBinding());

			GuiceIndex guiceIndex = GuiceIndex.get();

			IPackageBinding currentPackageBinding = astRoot.getPackage().resolveBinding();
			String packageToLookForGuiceModules = currentPackageBinding.getName();
			
			List<GuiceStatement> bindingStatements = guiceIndex.getBindingsByTypeAndAnnotationLimitToPackage(
					typeBindingWithoutProvider,
					guiceFieldDecl.getGuiceAnnotation(),
					SetUtils.newHashSet(packageToLookForGuiceModules));


			/*
			 * We do not show the proposal if the package module already
			 * contains a binding to this type. We ignore that bindings in
			 * not-package-modules might exist.
			 */
			if (bindingStatements.isEmpty()) {
				List<GuiceModule> guiceModulesInPackage = guiceIndex.getGuiceModulesInAndBelowPackage(currentPackageBinding);
				for (GuiceModule guiceModule : guiceModulesInPackage) {
					ProposalProviderMethodCreation proposal = new ProposalProviderMethodCreation(
							guiceModule,
							typeBindingWithoutProvider,
							guiceFieldDecl.getGuiceAnnotation(),
							guiceFieldDecl.getVariableName());
					proposals.add(proposal);
				}
			}
		}

	}

}
