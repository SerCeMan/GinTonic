package ru.naumen.gintonic.quickfix.module_creation;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import ru.naumen.gintonic.icons.Icons;
import ru.naumen.gintonic.select_and_reveal.SelectAndReveal;
import ru.naumen.gintonic.templates.GuiceModuleTemplate;
import ru.naumen.gintonic.utils.ICompilationUnitUtils;


public class ProposalCreateGuiceModule implements IJavaCompletionProposal {

	/**
	 * The package where we create the new guice module.
	 */
	private IPackageFragment packageFragment;

	private String guiceModuleClassName;

	public ProposalCreateGuiceModule(IPackageFragment packageFragment,
			String guiceModuleName) {
		this.packageFragment = packageFragment;
		this.guiceModuleClassName = guiceModuleName;
	}

	@Override
	public Point getSelection(IDocument document) {
		return null;
	}

	@Override
	public Image getImage() {
		return Icons.egapIconCreate;
	}

	/**
	 * Brief description of the action appears in the QuickfixProposal selection!
	 */
	@Override
	public String getDisplayString() {
		return "Create Guice module '" + guiceModuleClassName + "'";
	}

	/**
	 * Detailed description of the action, immersed in a window on the right 
	 * Next to the QuickfixProposal selection on!
	 */
	@Override
	public String getAdditionalProposalInfo() {
		return null;
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

	@Override
	public void apply(IDocument document) {
		GuiceModuleTemplate guiceModule = new GuiceModuleTemplate();
		String generatedCode = guiceModule.generate(guiceModuleClassName);
		String source = generatedCode;

		ICompilationUnit iCompilationUnit = ICompilationUnitUtils.createJavaCompilationUnit(
				packageFragment,
				guiceModuleClassName,
				source);

		SelectAndReveal.selectAndRevealPrimaryType(iCompilationUnit);

	}

	@Override
	public int getRelevance() {
		return 0;
	}

}
