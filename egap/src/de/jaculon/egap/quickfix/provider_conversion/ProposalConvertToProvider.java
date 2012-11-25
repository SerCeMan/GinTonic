package de.jaculon.egap.quickfix.provider_conversion;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import de.jaculon.egap.guice.GuiceConstants;
import de.jaculon.egap.icons.Icons;
import de.jaculon.egap.refactor.Refactorator;
import de.jaculon.egap.utils.IFileUtils;
import de.jaculon.egap.utils.TypeUtils;


public class ProposalConvertToProvider implements IJavaCompletionProposal {

	private final FieldDeclaration fieldDeclaration;
	private final ICompilationUnit icompilationUnit;
	private final CompilationUnit compilationUnit;

	public ProposalConvertToProvider(ICompilationUnit icompilationUnit,
			CompilationUnit compilationUnit,
			FieldDeclaration fieldDeclaration) {
		this.icompilationUnit = icompilationUnit;
		this.compilationUnit = compilationUnit;
		this.fieldDeclaration = fieldDeclaration;
	}

	@Override
	public Point getSelection(IDocument document) {
		return null;
	}

	@Override
	public Image getImage() {
		return Icons.egapDefaultIconSmall;
	}

	@Override
	/**
	 * Kurz Beschreibung der Aktion,taucht in der QuickfixProposal-Auswahl auf!
	 */
	public String getDisplayString() {
		return getAdditionalProposalInfo();
	}

	@Override
	/**
	 * Ausfuehrliche Beschreibung der Aktion, taucht in einem Fenster rechts
	 * neben der QuickfixProposal-Auswahl auf!
	 */
	public String getAdditionalProposalInfo() {
		Type type = fieldDeclaration.getType();
		String typeDeclarationAsString = TypeUtils.getTypeDeclarationAsString(type);
		String proposalInfo = "Convert to Provider<" + typeDeclarationAsString
				+ ">";
		return proposalInfo;
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

	@Override
	public void apply(IDocument document) {
		Refactorator refactorator = Refactorator.create(
				icompilationUnit,
				compilationUnit,
				fieldDeclaration.getAST());
		Type sourceType = fieldDeclaration.getType();
		refactorator.changeTypeByWrappingIt(
				sourceType,
				GuiceConstants.GUICE_PROVIDER);
		/* Change the variable name (e.g cat => catProvider) */
		refactorator.renameFieldIdentifiers(fieldDeclaration, "$Provider");
		refactorator.refactor(null);
		int startPosition = sourceType.getStartPosition();
		IFileUtils.setCaretAndRevealInEditor(
				(IFile) icompilationUnit.getResource(),
				startPosition);
	}

	@Override
	public int getRelevance() {
		return 0;
	}

}