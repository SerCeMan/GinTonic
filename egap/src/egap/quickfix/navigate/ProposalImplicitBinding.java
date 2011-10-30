package egap.quickfix.navigate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import egap.EgapPlugin;
import egap.icons.Icons;

public class ProposalImplicitBinding implements IJavaCompletionProposal {

	private final ITypeBinding typeBindingWithoutProvider;

	public ProposalImplicitBinding(ITypeBinding typeBindingWithoutProvider) {
		this.typeBindingWithoutProvider = typeBindingWithoutProvider;
	}

	@Override
	public void apply(IDocument document) {
		try {
			IJavaElement javaElement = typeBindingWithoutProvider.getJavaElement();
			JavaUI.openInEditor(javaElement);
		} catch (Exception e) {
			EgapPlugin.logException(e);
		}
	}

	@Override
	public Point getSelection(IDocument document) {
		return null;
	}

	@Override
	public String getAdditionalProposalInfo() {
		return null;
	}

	@Override
	public String getDisplayString() {
		return "Implicit binding, goto to "
				+ typeBindingWithoutProvider.getName();
	}

	@Override
	public Image getImage() {
		return Icons.egapDefaultIconSmall;
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

	@Override
	public int getRelevance() {
		return 0;
	}

}
