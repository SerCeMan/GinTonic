package de.jaculon.egap.quickfix.binding_creation;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import de.jaculon.egap.guice.GuiceConstants;
import de.jaculon.egap.guice.GuiceModule;
import de.jaculon.egap.icons.Icons;
import de.jaculon.egap.refactor.Refactorator;
import de.jaculon.egap.refactor.TrackedStatement;
import de.jaculon.egap.utils.ASTParserUtils;
import de.jaculon.egap.utils.IProjectResourceUtils;
import de.jaculon.egap.utils.MethodDeclarationUtils;


public class ProposalBindingCreation implements IJavaCompletionProposal {

	private final ITypeBinding sourceType;
	private final GuiceModule guiceModule;
	private final ITypeBinding interfaceBinding;
	private final boolean annotated;

	public ProposalBindingCreation(ITypeBinding sourceType,
			ITypeBinding interfaceBinding,
			GuiceModule guiceModule,
			boolean annotated) {
		this.sourceType = sourceType;
		this.interfaceBinding = interfaceBinding;
		this.guiceModule = guiceModule;
		this.annotated = annotated;
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
		String typeName = guiceModule.getTypeName();
		if (annotated) {
			String displayString = "Create an annotated binding to "
					+ interfaceBinding.getName() + " in "
					+ typeName + "";
			return displayString;
		}
		String displayString = "Create a linked binding to "
				+ interfaceBinding.getName() + " in "
				+ typeName + "";
		return displayString;

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
		ICompilationUnit compilationUnit = IProjectResourceUtils.getICompilationUnit(guiceModule);
		CompilationUnit compilationUnitAstNode = ASTParserUtils.parseCompilationUnitAst3(compilationUnit);
		final Refactorator refactorator = Refactorator.create(
				compilationUnit,
				compilationUnitAstNode,
				compilationUnitAstNode.getAST());

		refactorator.addImport(sourceType);
		refactorator.addImport(interfaceBinding);

		final boolean isParameterizedType = interfaceBinding.isParameterizedType();
		if (isParameterizedType) {
			refactorator.addImport(GuiceConstants.GUICE_TYPE_LITERAL);
		}

		MethodDeclaration configureMethodDeclaration = MethodDeclarationUtils.getConfigureMethodDeclaration(compilationUnitAstNode);
		if (configureMethodDeclaration != null) {
			String statementAsString;
			if (isParameterizedType) {
				statementAsString = "bind(new TypeLiteral<"
						+ interfaceBinding.getName() + ">(){})";
			}
			else {
				statementAsString = "bind(" + interfaceBinding.getName()
						+ ".class)";
			}

			if (annotated) {
				statementAsString = statementAsString + ".annotatedWith(X)";
			}

			statementAsString += ".to(" + sourceType.getName() + ".class);";

			TrackedStatement trackedStatement = refactorator.addAsLastStatementInMethod(
					configureMethodDeclaration,
					statementAsString);
			
			refactorator.refactor(null);
			int startPosition = trackedStatement.getStartPosition();
			IProjectResourceUtils.openEditorWithStatementDeclaration(
					guiceModule, startPosition);
		}

	}

	@Override
	public int getRelevance() {
		return 0;
	}

}
