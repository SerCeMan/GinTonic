package ru.naumen.gintonic.context.quickfix;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

public interface GinTonicQuickFix {

	void addProposals(IInvocationContext context, List<IJavaCompletionProposal> proposals)
			throws CoreException;

}
