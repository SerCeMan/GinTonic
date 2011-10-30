package egap.quickfix.module_creation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import egap.icons.Icons;
import egap.templates.GuiceModuleTemplate;
import egap.utils.EditorUtils;
import egap.utils.ICompilationUnitUtils;
import egap.utils.IFileUtils;

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

	@Override
	/**
	 * Kurz Beschreibung der Aktion,taucht in der QuickfixProposal-Auswahl auf!
	 */
	public String getDisplayString() {
		return "Create Guice module '" + guiceModuleClassName + "'";
	}

	@Override
	/**
	 * Ausfuehrliche Beschreibung der Aktion, taucht in einem Fenster rechts
	 * neben der QuickfixProposal-Auswahl auf!
	 */
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

		ICompilationUnitUtils.viewInEditor(iCompilationUnit);

	}

	@Override
	public int getRelevance() {
		return 0;
	}

}
