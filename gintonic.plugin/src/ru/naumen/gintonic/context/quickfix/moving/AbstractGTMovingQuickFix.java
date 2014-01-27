package ru.naumen.gintonic.context.quickfix.moving;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

import ru.naumen.gintonic.context.quickfix.AbstractGinTonicQuickFix;
import ru.naumen.gintonic.guice.GuiceIndex;
import ru.naumen.gintonic.guice.injection.InjectionPoint;
import ru.naumen.gintonic.guice.injection.InjectionPointDao;
import ru.naumen.gintonic.guice.statements.BindingDefinition;

public abstract class AbstractGTMovingQuickFix extends AbstractGinTonicQuickFix {

    @Override
    public final void addProposals(IInvocationContext context, List<IJavaCompletionProposal> proposals) throws CoreException {
        CompilationUnit astRoot = context.getASTRoot();

        InjectionPointDao injectionPointDao = new InjectionPointDao();
        InjectionPoint injectionPoint = injectionPointDao.getGuiceFieldDeclarationIfFieldDeclaration(context.getCoveringNode(), astRoot);

        if (injectionPoint != null) {
            GuiceIndex guiceIndex = GuiceIndex.get();
            List<BindingDefinition> bindingDefinitions = guiceIndex.getBindingDefinitions(injectionPoint);
            addMovingProposal(proposals, injectionPoint, bindingDefinitions);
        }
    }
    
    protected abstract void addMovingProposal(List<IJavaCompletionProposal> proposals, 
            InjectionPoint injectionPoint,
            List<BindingDefinition> bindingDefinitions);
}
