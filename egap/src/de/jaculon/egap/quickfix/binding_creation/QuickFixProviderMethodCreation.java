package de.jaculon.egap.quickfix.binding_creation;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

import de.jaculon.egap.guice.GuiceIndex;
import de.jaculon.egap.guice.GuiceModule;
import de.jaculon.egap.guice.injection_point.InjectionPoint;
import de.jaculon.egap.guice.injection_point.InjectionPointDao;
import de.jaculon.egap.guice.statements.BindingDefinition;
import de.jaculon.egap.quickfix.AbstractEgapQuickFix;
import de.jaculon.egap.utils.ITypeBindingUtils;
import de.jaculon.egap.utils.SetUtils;



/**
 * @author tmajunke
 */
public class QuickFixProviderMethodCreation extends AbstractEgapQuickFix {

	@Override
	public void addProposals(IInvocationContext context,
			List<IJavaCompletionProposal> proposals) throws CoreException {
		CompilationUnit astRoot = context.getASTRoot();

		InjectionPointDao injectionPointDao = new InjectionPointDao();
		InjectionPoint injectionPoint = injectionPointDao.getGuiceFieldDeclarationIfFieldDeclaration(context.getCoveringNode(), astRoot);

		if (injectionPoint != null) {
			ITypeBinding typeBindingWithoutProvider = ITypeBindingUtils.removeSurroundingProvider(injectionPoint.getTargetTypeBinding());

			GuiceIndex guiceIndex = GuiceIndex.get();

			IPackageBinding currentPackageBinding = astRoot.getPackage().resolveBinding();
			String packageToLookForGuiceModules = currentPackageBinding.getName();

			List<BindingDefinition> bindingStatements = guiceIndex.getBindingsByTypeAndAnnotationLimitToPackage(
					typeBindingWithoutProvider,
					injectionPoint.getGuiceAnnotation(),
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
							injectionPoint.getGuiceAnnotation(),
							injectionPoint.getVariableName());
					proposals.add(proposal);
				}
			}
		}

	}

}
