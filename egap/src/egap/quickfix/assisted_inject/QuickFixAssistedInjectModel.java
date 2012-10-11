package egap.quickfix.assisted_inject;

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

import egap.guice.GuiceIndex;
import egap.guice.GuiceModule;
import egap.guice.statements.BindingStatement;
import egap.quickfix.AbstractEgapQuickFix;
import egap.quickfix.navigate.ProposalNavigateToStatement;
import egap.utils.ASTNodeUtils;
import egap.utils.ICompilationUnitUtils;
import egap.utils.MethodDeclarationUtils;

/**
 * @author tmajunke
 */
public class QuickFixAssistedInjectModel extends AbstractEgapQuickFix {
	
	public static final String ENABLED_STATE_ID = "QuickFixCreateAssistedFactory.ENABLED_STATE";
	
	@Override
	public String getPreferencesDisplayName() {
		return "Assisted Inject Quickfixes";
	}

	@Override
	public String getEnabledStateID() {
		return ENABLED_STATE_ID;
	}
	
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
		 * Wir leiten den Namen der Factory aus der Compilation Unit ab (z.B
		 * Person => PersonFactory)
		 */
		String modelTypeName = modelType.getElementName();
		String factoryTypeName = modelTypeName + "Factory";
		
		ICompilationUnit factory = modelPackage.getCompilationUnit(factoryTypeName
				+ ICompilationUnitUtils.JAVA_EXTENSION);
		
		if (factory.exists()) {
			GuiceIndex guiceIndex = GuiceIndex.get();

			/* Does a binding exist? */
			String fullyQualifiedName = modelType.getFullyQualifiedName();
			BindingStatement bindStatement = guiceIndex.getAssistedBindingByModelType(fullyQualifiedName);

			if (bindStatement != null) {
				ProposalNavigateToStatement gotoGuiceStatementProposal = new ProposalNavigateToStatement(
						bindStatement);
				proposals.add(gotoGuiceStatementProposal);
			} else {
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
