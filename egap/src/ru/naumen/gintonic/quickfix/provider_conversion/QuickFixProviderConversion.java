package ru.naumen.gintonic.quickfix.provider_conversion;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

import ru.naumen.gintonic.guice.injection_point.InjectionPoint;
import ru.naumen.gintonic.guice.injection_point.InjectionPointDao;
import ru.naumen.gintonic.quickfix.AbstractEgapQuickFix;
import ru.naumen.gintonic.utils.ITypeBindingUtils;


/**
 * @author tmajunke
 */
public class QuickFixProviderConversion extends AbstractEgapQuickFix {

	@Override
	public void addProposals(IInvocationContext context,
			List<IJavaCompletionProposal> proposals) throws CoreException {

		InjectionPointDao injectionPointDao = new InjectionPointDao();
		InjectionPoint guiceFieldDeclaration = injectionPointDao.getGuiceFieldDeclarationIfFieldDeclaration(context.getCoveringNode(), context.getASTRoot());

		if (guiceFieldDeclaration == null) {
			return;
		}

		ITypeBinding typeBinding = guiceFieldDeclaration.getTargetTypeBinding();

		/* Check if the declaration is already a provider declaration. */
		boolean isProviderType =  ITypeBindingUtils.isGuiceProviderType(typeBinding);

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

}
