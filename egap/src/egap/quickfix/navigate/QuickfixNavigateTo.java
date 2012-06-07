package egap.quickfix.navigate;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

import egap.guice.GuiceIndex;
import egap.guice.annotations.GuiceAnnotation;
import egap.guice.statements.GuiceStatement;
import egap.quickfix.AbstractEgapQuickFix;
import egap.utils.ASTNodeUtils;
import egap.utils.GuiceTypeInfo;
import egap.utils.GuiceTypeWithAnnotation;
import egap.utils.ITypeBindingUtils;

/**
 * Enables us to jump from an inject declaration to the inject binding
 * declaration.
 * 
 * <h5>Example:</h5>
 * 
 * <pre>
 * <code>
 * class Person{}
 * 
 * class PersonModule{
 *   public void configure(){
 *   	bind(Person.class).in(Scopes.SINGLETON);
 *   }
 * }
 * 
 * class Barmixer{
 *  @Inject
 * 	private Person person;
 * }
 * 
 * </code>
 * </pre>
 * 
 * The quick fix enables us to jump from person (the inject declaration) to the
 * binding statement in PersonModule.
 * 
 * 
 * @author tmajunke
 */
public class QuickfixNavigateTo extends AbstractEgapQuickFix {

	@Override
	public void addProposals(IInvocationContext context,
			List<IJavaCompletionProposal> proposals) throws CoreException {
		GuiceTypeInfo guiceTypeInfo = ASTNodeUtils.getGuiceTypeInfoIfFieldDeclarationTypeDeclarationOrProviderMethod(
				context.getCoveringNode(),
				context.getASTRoot(),
				context.getCompilationUnit());

		if (guiceTypeInfo != null) {
			addGotoProposalFrom(
					guiceTypeInfo,
					proposals);
		}

	}

	private void addGotoProposalFrom(GuiceTypeInfo guiceTypeInfo,
			List<IJavaCompletionProposal> proposals) {
		GuiceIndex guiceIndex = GuiceIndex.get();
		ITypeBinding typeBinding = guiceTypeInfo.getTargetTypeBinding();
		ITypeBinding typeBindingWithoutProvider = ITypeBindingUtils.removeSurroundingProvider(typeBinding);
		
		List<GuiceStatement> bindingStatements = null;
		
		if (guiceTypeInfo instanceof GuiceTypeWithAnnotation) {
			GuiceTypeWithAnnotation annotatedThing = (GuiceTypeWithAnnotation) guiceTypeInfo;
			GuiceAnnotation guiceAnnotation = annotatedThing.getGuiceAnnotation();
			bindingStatements = guiceIndex.getBindingsByTypeAndAnnotation(
					typeBindingWithoutProvider,
					guiceAnnotation);
		}else{
			/* We only have the type. */
			bindingStatements = guiceIndex.getBindingsByType(
					typeBindingWithoutProvider);
		}
		
		createProposalForEachBinding(proposals, bindingStatements);

	}

	public void createProposalForEachBinding(
			List<IJavaCompletionProposal> proposals,
			List<GuiceStatement> bindingStatements) {
		for (GuiceStatement binding : bindingStatements) {
			ProposalNavigateToStatement proposal = new ProposalNavigateToStatement(
					binding);
			proposals.add(proposal);
		}
	}

	@Override
	public String getPreferencesDisplayName() {
		return "Goto";
	}

	public static final String ENABLED_STATE_ID = "QuickfixNavigateTo.ENABLED_STATE";

	@Override
	public String getEnabledStateID() {
		return ENABLED_STATE_ID;
	}
}
