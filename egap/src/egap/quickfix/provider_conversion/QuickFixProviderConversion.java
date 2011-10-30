package egap.quickfix.provider_conversion;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

import egap.guice.ProjectResource;
import egap.quickfix.AbstractEgapQuickFix;
import egap.utils.ASTNodeUtils;
import egap.utils.GuiceFieldDeclaration;
import egap.utils.IProjectResourceUtils;
import egap.utils.ITypeBindingUtils;

/**
 * @author tmajunke
 */
public class QuickFixProviderConversion extends AbstractEgapQuickFix {

	@Override
	public void addProposals(IInvocationContext context,
			List<IJavaCompletionProposal> proposals) throws CoreException {
		
		ProjectResource origin = IProjectResourceUtils.createNavigationEndpoint(
				context.getCoveringNode(),
				context.getASTRoot(),
				context.getCompilationUnit());
		
		GuiceFieldDeclaration guiceFieldDeclaration = ASTNodeUtils.getGuiceFieldDeclarationIfFieldDeclaration(
				origin,
				context.getCoveringNode(),
				context.getASTRoot());
		
		if (guiceFieldDeclaration == null) {
			return;
		}
		
		ITypeBinding typeBinding = guiceFieldDeclaration.getTargetTypeBinding();
		
		/* Check if the declaration is already a provider declaration. */
		boolean isProviderType =  ITypeBindingUtils.isProviderType(typeBinding);

		FieldDeclaration fieldDeclaration = guiceFieldDeclaration.getFieldDeclaration();
		if (isProviderType) {
			
			/**
			 * 
			 * <pre>
			 * <code>Provider<Cat> 
			 * providerType = Provider<Cat> 
			 * providedType = Cat
			 * </code>
			 * </pre>
			 */
			ParameterizedType providerType = (ParameterizedType) fieldDeclaration.getType();
			@SuppressWarnings("unchecked")
			List<Type> typeArguments = providerType.typeArguments();
			Type providedType = typeArguments.get(0);
			
			ProposalRemoveProvider proposalRemove = new ProposalRemoveProvider(
					context.getCompilationUnit(),
					context.getASTRoot(),
					fieldDeclaration,
					providerType,
					providedType);
			
			proposals.add(proposalRemove);
		}else{
			/*
			 * Now we know that its a field declaration annotated with @Inject. We
			 * also checked that it's not a Provider declaration. So we can provide
			 * a proposal for it.
			 */
			ProposalConvertToProvider proposalConvertToProvider = new ProposalConvertToProvider(
					context.getCompilationUnit(),
					context.getASTRoot(),
					fieldDeclaration);
			
			proposals.add(proposalConvertToProvider);
		}
		

	}

	@Override
	public String getPreferencesDisplayName() {
		return "Provider conversion";
	}

	public static final String ENABLED_STATE_ID = "QuickFixProviderConversion.ENABLED_STATE";

	@Override
	public String getEnabledStateID() {
		return ENABLED_STATE_ID;
	}

}
