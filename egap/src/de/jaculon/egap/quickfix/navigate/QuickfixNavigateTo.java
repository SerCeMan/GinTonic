package de.jaculon.egap.quickfix.navigate;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

import de.jaculon.egap.guice.GuiceIndex;
import de.jaculon.egap.guice.annotations.GuiceAnnotation;
import de.jaculon.egap.guice.statements.GuiceStatement;
import de.jaculon.egap.quickfix.AbstractEgapQuickFix;
import de.jaculon.egap.utils.ASTNodeUtils;
import de.jaculon.egap.utils.IAnnotatedInjectionPoint;
import de.jaculon.egap.utils.IInjectionPoint;
import de.jaculon.egap.utils.ITypeBindingUtils;


/**
 * Enables us to jump from an injection point to its binding definition.
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
 * The quick fix enables us to jump from person (the injection point) to the
 * binding statement in PersonModule.
 *
 *
 * @author tmajunke
 */
public class QuickfixNavigateTo extends AbstractEgapQuickFix {

	@Override
	public void addProposals(IInvocationContext context,
			List<IJavaCompletionProposal> proposals) throws CoreException {
		IInjectionPoint injectionPoint = ASTNodeUtils.getInjectionPoint(
				context.getCoveringNode(),
				context.getASTRoot());

		if (injectionPoint != null) {
			addGotoProposalFrom(
					injectionPoint,
					proposals);
		}

	}

	private void addGotoProposalFrom(IInjectionPoint injectionPoint,
			List<IJavaCompletionProposal> proposals) {
		GuiceIndex guiceIndex = GuiceIndex.get();
		ITypeBinding typeBinding = injectionPoint.getTargetTypeBinding();
		ITypeBinding typeBindingWithoutProvider = ITypeBindingUtils.removeSurroundingProvider(typeBinding);

		List<GuiceStatement> bindingStatements = null;

		if (injectionPoint instanceof IAnnotatedInjectionPoint) {
			IAnnotatedInjectionPoint annotatedThing = (IAnnotatedInjectionPoint) injectionPoint;
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

}
