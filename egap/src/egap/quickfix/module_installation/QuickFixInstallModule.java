package egap.quickfix.module_installation;

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
import egap.guice.statements.InstallModuleStatement;
import egap.quickfix.AbstractEgapQuickFix;
import egap.quickfix.navigate.ProposalNavigateToStatement;
import egap.utils.ASTNodeUtils;
import egap.utils.StringUtils;

/**
 * Enables the user to install the selected guice module (the source module) in
 * another guice module (the target module).
 * 
 * <h5>How is the quick fix activated?</h5>
 * 
 * The quick fix is triggered if the covering node implements
 * {@link StringUtils#GUICE_MODULE}
 * 
 * <h5>How do you discover the target module?</h5>
 * 
 * We collect all modules in the package of the source module and in the parent
 * package. A proposal ("Install SourceModule in TargetModule") is shown for
 * every discovered target module.
 * 
 * @author tmajunke
 */
public class QuickFixInstallModule extends AbstractEgapQuickFix {

	@Override
	public void addProposals(IInvocationContext context,
			List<IJavaCompletionProposal> proposals) throws CoreException {

		ASTNode coveringNode = context.getCoveringNode();
		TypeDeclaration typeDeclaration = ASTNodeUtils.getTypeDeclaration(coveringNode);
		if(typeDeclaration == null){
			return;
		}
		ITypeBinding guiceModuleSource = ASTNodeUtils.getTypeBindingIfGuiceModuleCovered(coveringNode);
		if (guiceModuleSource == null) {
			return;
		}
		GuiceIndex guiceIndex = GuiceIndex.get();
		IPackageBinding currentPackageBinding = guiceModuleSource.getPackage();
		List<GuiceModule> guiceModules = guiceIndex.getGuiceModulesInAndBelowPackage(
				currentPackageBinding,
				guiceModuleSource.getQualifiedName(),
				2);
		if (guiceModules.isEmpty()) {
			return;
		}

		/**
		 * Create a proposal for each target module.
		 */
		for (GuiceModule guiceModuleTarget : guiceModules) {

			/* Check if the module is already installed */
			List<InstallModuleStatement> installedModules = guiceModuleTarget.getInstalledModules();

			if (installedModules != null) {
				String qualifiedName = guiceModuleSource.getQualifiedName();

				boolean alreadyInstalled = false;

				for (InstallModuleStatement installModuleStatement : installedModules) {
					String moduleName = installModuleStatement.getModuleNameFullyQualified();
					if (moduleName.equals(qualifiedName)) {
						ProposalNavigateToStatement gotoModule = new ProposalNavigateToStatement(
								installModuleStatement);
						proposals.add(gotoModule);
						alreadyInstalled = true;
					}
				}

				if (!alreadyInstalled) {
					addInstallModuleProposal(
							proposals,
							guiceModuleSource,
							guiceModuleTarget);
				}
			} else {
				addInstallModuleProposal(
						proposals,
						guiceModuleSource,
						guiceModuleTarget);
			}

		}

	}

	private void addInstallModuleProposal(
			List<IJavaCompletionProposal> proposals,
			ITypeBinding guiceModuleSource, GuiceModule guiceModuleTarget) {
		ProposalInstallModule proposalInstallModule = new ProposalInstallModule(
				guiceModuleSource,
				guiceModuleTarget);
		proposals.add(proposalInstallModule);
	}

	@Override
	public String getPreferencesDisplayName() {
		return "Install Guice module in parent module";
	}

	@Override
	public String getEnabledStateID() {
		return EgapPlugin.ID_QUICKFIXINSTALLMODULE_ENABLED_STATE;
	}

}
