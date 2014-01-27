package ru.naumen.gintonic.context.quickfix.moving;

import java.util.List;

import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

import ru.naumen.gintonic.guice.injection.InjectionPoint;
import ru.naumen.gintonic.guice.statements.BindingDefinition;

/**
 * @author Sergey Tselovalnikov
 * @since Jan 27, 2014
 */
public class QuickFixGoToImpl extends AbstractGTMovingQuickFix {

    @Override
    protected void addMovingProposal (List<IJavaCompletionProposal> proposals, InjectionPoint injectionPoint,
            List<BindingDefinition> bindingDefinitions) {
        proposals.add(new ProposalGoToImpl(bindingDefinitions, injectionPoint));
    }
}
