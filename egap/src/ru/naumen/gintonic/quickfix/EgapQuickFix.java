package ru.naumen.gintonic.quickfix;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

public interface EgapQuickFix {

	void addProposals(IInvocationContext context, List<IJavaCompletionProposal> proposals)
			throws CoreException;

}
