package egap.quickfix.navigate;

import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import egap.guice.statements.GuiceStatement;
import egap.icons.Icons;
import egap.utils.IProjectResourceUtils;

public class ProposalNavigateToStatement implements IJavaCompletionProposal {

	private final GuiceStatement guiceStatement;

	public ProposalNavigateToStatement(GuiceStatement guiceStatement) {
		this.guiceStatement = guiceStatement;
	}

	@Override
	public Point getSelection(IDocument document) {
		return null;
	}

	@Override
	public Image getImage() {
		return Icons.egapIconGoto;
	}

	@Override
	/**
	 * Kurz Beschreibung der Aktion,taucht in der QuickfixProposal-Auswahl auf!
	 */
	public String getDisplayString() {
		return guiceStatement.getLabel();
	}

	@Override
	/**
	 * Ausfuehrliche Beschreibung der Aktion, taucht in einem Fenster rechts
	 * neben der QuickfixProposal-Auswahl auf!
	 */
	public String getAdditionalProposalInfo() {
		return getDisplayString();
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

	@Override
	public void apply(IDocument document) {
		IProjectResourceUtils.openEditorWithStatementDeclaration(guiceStatement);
	}

	@Override
	public int getRelevance() {
		return 0;
	}
	
}