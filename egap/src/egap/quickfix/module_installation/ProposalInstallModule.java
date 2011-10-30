package egap.quickfix.module_installation;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import egap.guice.GuiceModule;
import egap.icons.Icons;
import egap.refactor.Refactorator;
import egap.templates.InstallModuleTemplate;
import egap.utils.ASTParserUtils;
import egap.utils.IProjectResourceUtils;

public class ProposalInstallModule implements IJavaCompletionProposal {

	private final class AddInstallModuleStatement extends ASTVisitor {

		private final Refactorator refactorator;

		private AddInstallModuleStatement(Refactorator refactorator) {
			this.refactorator = refactorator;
		}

		@SuppressWarnings("synthetic-access")
		@Override
		public boolean visit(MethodDeclaration method) {
			SimpleName simpleName = method.getName();
			String methodNameAsString = simpleName.toString();
			if (methodNameAsString.equals("configure")) {
				InstallModuleTemplate binding = new InstallModuleTemplate();
				String installModuleStatement = binding.generate(guiceModuleSource.getName());
				refactorator.addAsLastStatementInMethod(method, installModuleStatement);
			}

			return super.visit(method);
		}
	}

	private ITypeBinding guiceModuleSource;
	private GuiceModule guiceModuleTarget;

	public ProposalInstallModule(ITypeBinding guiceModuleSource,
			GuiceModule guiceModuleTarget) {
		this.guiceModuleSource = guiceModuleSource;
		this.guiceModuleTarget = guiceModuleTarget;
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
		return "Install '" + guiceModuleSource.getName() + "' in '"
				+ guiceModuleTarget.getTypeName() + "'";
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
		ICompilationUnit compilationUnit = IProjectResourceUtils.getICompilationUnit(guiceModuleTarget);
		CompilationUnit compilationUnitAstNode = ASTParserUtils.parseCompilationUnitAst3(compilationUnit);
		Refactorator refactorator = Refactorator.create(compilationUnit, compilationUnitAstNode, compilationUnitAstNode.getAST());
		refactorator.addImport(guiceModuleSource.getQualifiedName());
		compilationUnitAstNode.accept(new AddInstallModuleStatement(refactorator));
		refactorator.refactor(null);
	}

	@Override
	public int getRelevance() {
		return 0;
	}

}