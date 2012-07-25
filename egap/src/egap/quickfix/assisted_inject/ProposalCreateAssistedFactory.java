package egap.quickfix.assisted_inject;

import java.util.List;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;


import egap.icons.Icons;
import egap.source_builder.JavaCodeBuilder;
import egap.source_formatter.JavaSourceFormatter;
import egap.utils.ICompilationUnitUtils;
import egap.utils.MethodDeclarationUtils;

public class ProposalCreateAssistedFactory implements IJavaCompletionProposal {

	/**
	 * The package where we store the new factory class.
	 */
	private final IPackageFragment packageFragment;

	/**
	 * The name of the factory class.
	 */
	private final String factoryInterfaceName;

	/**
	 * The target class which our new constructor is going to create.
	 */
	private final IType targetClassType;

	private final MethodDeclaration constructor;

	public ProposalCreateAssistedFactory(MethodDeclaration constructor,
			IPackageFragment packageFragment,
			String factoryClassName,
			IType targetClassType) {
		this.constructor = constructor;
		this.packageFragment = packageFragment;
		this.factoryInterfaceName = factoryClassName;
		this.targetClassType = targetClassType;
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
		return "Create Assisted Inject Interface '" + factoryInterfaceName
				+ "'";
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

		StringBuffer sb = new StringBuffer(2000);
		JavaCodeBuilder codeGenerator = new JavaCodeBuilder(sb);
		
		List<SingleVariableDeclaration> variableDeclAnnotatedWithAssisted = MethodDeclarationUtils.getVariableDeclAnnotatedWithAssisted(constructor);
		codeGenerator.addImports(
				targetClassType,
				variableDeclAnnotatedWithAssisted);
		
		codeGenerator.startInterface(factoryInterfaceName);
		codeGenerator.startBlock();
		String targetClassName = targetClassType.getElementName();
		
		codeGenerator.startMethod(
				null,
				"public",
				"create",
				targetClassName,
				variableDeclAnnotatedWithAssisted,
				"Creates a new {@link " + targetClassName + "}.");
		codeGenerator.finishStatement();
		codeGenerator.finishBlock();
		
		String code = JavaSourceFormatter.format(sb.toString());
		String sourceCode = code;

		ICompilationUnitUtils.createJavaCompilationUnit(
				packageFragment,
				factoryInterfaceName,
				sourceCode);

	}

	@Override
	public int getRelevance() {
		return 0;
	}

}