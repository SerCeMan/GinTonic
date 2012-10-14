package egap.quickfix.binding_creation;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

import egap.EgapPlugin;
import egap.guice.GuiceIndex;
import egap.guice.GuiceModule;
import egap.guice.statements.GuiceStatement;
import egap.quickfix.AbstractEgapQuickFix;
import egap.utils.ASTNodeUtils;
import egap.utils.ITypeBindingUtils;

/**
 * Enables the user to create a linked binding for the selected type (the
 * source module) in a nearby guice module (the target module).
 * 
 * <h5>How is the quick fix activated?</h5>
 * 
 * <h5>How do you discover the target module?</h5>
 * 
 * @author tmajunke
 */
public class QuickFixBindingCreation extends AbstractEgapQuickFix {

	@Override
	public void addProposals(IInvocationContext context,
			List<IJavaCompletionProposal> proposals) throws CoreException {

		ASTNode coveringNode = context.getCoveringNode();
		TypeDeclaration typeDeclaration = ASTNodeUtils.getTypeDeclaration(coveringNode);
		if (typeDeclaration == null) {
			return;
		}
		ITypeBinding sourceType = ASTNodeUtils.getTypeBindingIfNotAGuiceModule(coveringNode);
		if (sourceType == null) {
			return;
		}
		GuiceIndex guiceIndex = GuiceIndex.get();
		IPackageBinding currentPackageBinding = sourceType.getPackage();

		if (!ITypeBindingUtils.isConcreteType(sourceType)) {
			return;
		}

		List<GuiceModule> guiceModulesInAndBelowPackage = guiceIndex.getGuiceModulesInAndBelowPackage(
				currentPackageBinding);

		if (guiceModulesInAndBelowPackage.size() == 0) {
			return;
		}

		ITypeBinding[] interfaces = sourceType.getInterfaces();
		for (ITypeBinding interfaceBinding : interfaces) {
			List<GuiceStatement> bindingStatements = guiceIndex.getBindingsByTypeAndAnnotation(
					interfaceBinding,
					null);

			if (bindingStatements.size() == 0) {
				/*
				 * No binding at all found and we get a target module where to
				 * create the binding.
				 */

				for (GuiceModule guiceModule : guiceModulesInAndBelowPackage) {
					ProposalBindingCreation proposalBindingCreation = new ProposalBindingCreation(
							sourceType,
							interfaceBinding,
							guiceModule,
							false);
					proposals.add(proposalBindingCreation);
				}

			}
			else {
				for (GuiceModule guiceModule : guiceModulesInAndBelowPackage) {
					ProposalBindingCreation proposalBindingCreation = new ProposalBindingCreation(
							sourceType,
							interfaceBinding,
							guiceModule,
							false);
					proposals.add(proposalBindingCreation);
				}
			}
		}

	}

	@Override
	public String getPreferencesDisplayName() {
		return "Create a Linked Binding";
	}

	@Override
	public String getEnabledStateID() {
		return EgapPlugin.ID_QUICKFIXBINDINGCREATION_ENABLED_STATE;
	}

}
