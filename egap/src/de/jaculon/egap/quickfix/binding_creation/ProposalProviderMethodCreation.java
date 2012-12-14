package de.jaculon.egap.quickfix.binding_creation;

import java.util.Map;

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
import de.jaculon.egap.guice.annotations.GuiceAnnotation;
import de.jaculon.egap.guice.annotations.GuiceClassAnnotation;
import de.jaculon.egap.icons.Icons;
import de.jaculon.egap.project_resource.IProjectResourceUtils;
import de.jaculon.egap.refactor.Refactorator;
import de.jaculon.egap.refactor.TrackedMethodDeclaration;
import de.jaculon.egap.templates.ProviderMethodTemplate;
import de.jaculon.egap.utils.ASTParserUtils;
import de.jaculon.egap.utils.MapUtils;
import de.jaculon.egap.utils.MethodDeclarationUtils;
import de.jaculon.egap.utils.Preconditions;



public class ProposalProviderMethodCreation implements IJavaCompletionProposal {

	private final GuiceModule guiceModule;
	private final ITypeBinding type;
	private final GuiceAnnotation guiceAnnotation;
	private final String variableName;

	public ProposalProviderMethodCreation(GuiceModule guiceModule,
			ITypeBinding typeBindingWithoutProvider,
			GuiceAnnotation guiceAnnotation,
			String variableName) {
		Preconditions.checkNotNull(guiceModule);
		Preconditions.checkNotNull(typeBindingWithoutProvider);
		Preconditions.checkNotNull(variableName);
		this.guiceModule = guiceModule;
		this.type = typeBindingWithoutProvider;
		this.guiceAnnotation = guiceAnnotation;
		this.variableName = variableName;
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
		return "Create provider method in " + guiceModule.getPrimaryTypeName();

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
		
		ICompilationUnit compilationUnit = IProjectResourceUtils.getICompilationUnit(guiceModule.getSourceCodeReference());
		CompilationUnit compilationUnitAstNode = ASTParserUtils.parseCompilationUnitAst3(compilationUnit);
		final Refactorator refactorator = Refactorator.create(
				compilationUnit,
				compilationUnitAstNode,
				compilationUnitAstNode.getAST());

		refactorator.addImport(GuiceConstants.PROVIDES);
		refactorator.addImport(type.getQualifiedName());
		
		if (guiceAnnotation != null) {
			refactorator.addImport(guiceAnnotation.getTypeToImport());
		}
		
		MethodDeclaration configureMethodDeclaration = MethodDeclarationUtils.getConfigureMethodDeclaration(compilationUnitAstNode);
		if (configureMethodDeclaration != null) {
			String providerMethodDecl = getMethodCode();
			TrackedMethodDeclaration trackedMethodDeclaration = refactorator.addMethod(providerMethodDecl);
			refactorator.refactor(null);
			
			int startPosition = trackedMethodDeclaration.getStartPosition();
			IProjectResourceUtils.openEditorWithStatementDeclaration(
					guiceModule.getSourceCodeReference(),
					startPosition);
		}
		
	}

	/**
	 * @return
	 */
	private String getMethodCode() {
		ProviderMethodTemplate template = new ProviderMethodTemplate();
		Map<String, String> arguments = MapUtils.newHashMap();
		arguments.put("type", type.getName());
		
		if (guiceAnnotation != null && guiceAnnotation instanceof GuiceClassAnnotation) {
			GuiceClassAnnotation classAnnotation = (GuiceClassAnnotation) guiceAnnotation;
			arguments.put("annotation", "@" + classAnnotation.getName());
		}
		
		arguments.put("variablename", variableName);
		String providerMethodDecl = template.generate(arguments);
		return providerMethodDecl;
	}

	@Override
	public int getRelevance() {
		return 0;
	}

}
