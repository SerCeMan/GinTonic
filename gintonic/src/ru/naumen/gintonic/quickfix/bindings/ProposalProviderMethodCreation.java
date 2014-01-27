package ru.naumen.gintonic.quickfix.bindings;

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

import ru.naumen.gintonic.context.refactor.Refactorator;
import ru.naumen.gintonic.context.refactor.TrackedMethodDeclaration;
import ru.naumen.gintonic.guice.GuiceConstants;
import ru.naumen.gintonic.guice.GuiceModule;
import ru.naumen.gintonic.guice.annotations.GuiceClassAnnotation;
import ru.naumen.gintonic.guice.annotations.IGuiceAnnotation;
import ru.naumen.gintonic.icons.Icons;
import ru.naumen.gintonic.project.source.references.SourceCodeReference;
import ru.naumen.gintonic.templates.ProviderMethodTemplate;
import ru.naumen.gintonic.utils.ASTParserUtils;
import ru.naumen.gintonic.utils.MapUtils;
import ru.naumen.gintonic.utils.MethodDeclarationUtils;
import ru.naumen.gintonic.utils.Preconditions;



public class ProposalProviderMethodCreation implements IJavaCompletionProposal {

	private final GuiceModule guiceModule;
	private final ITypeBinding type;
	private final IGuiceAnnotation guiceAnnotation;
	private final String variableName;

	public ProposalProviderMethodCreation(GuiceModule guiceModule,
			ITypeBinding typeBindingWithoutProvider,
			IGuiceAnnotation guiceAnnotation,
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
		return Icons.ginTonicIconCreate;
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
		
		SourceCodeReference sourceCodeReference = guiceModule.getSourceCodeReference();
		ICompilationUnit compilationUnit = sourceCodeReference.resolveICompilationUnit();
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
			sourceCodeReference.jump(startPosition);
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
