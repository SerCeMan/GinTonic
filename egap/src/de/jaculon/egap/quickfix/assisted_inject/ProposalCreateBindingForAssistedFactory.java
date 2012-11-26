package de.jaculon.egap.quickfix.assisted_inject;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import de.jaculon.egap.guice.GuiceConstants;
import de.jaculon.egap.guice.GuiceModule;
import de.jaculon.egap.icons.Icons;
import de.jaculon.egap.project_resource.IProjectResourceUtils;
import de.jaculon.egap.refactor.Refactorator;
import de.jaculon.egap.templates.GuiceAssistedInjectFactoryBinding;
import de.jaculon.egap.utils.ASTParserUtils;


/**
 * <h5>Example:</h5>
 * 
 * <pre>
 * <code>
 * bind(RealPaymentFactory.class).toProvider(
 * 	FactoryProvider.newFactory(
 * 	RealPaymentFactory.class,
 * 	RealPayment.class));
 * </code>
 * </pre>
 * 
 * @author tmajunke
 */
public class ProposalCreateBindingForAssistedFactory implements
		IJavaCompletionProposal {

	/**
	 * The guice model where the binding statement is inserted. 
	 */
	private final GuiceModule guiceModule;
	
	/**
	 * The factory we bind(RealPaymentFactory in the example).. 
	 */
	private final IType factoryType;

	/**
	 * The model that is bound (RealPayment in the example).
	 */
	private final IType modelType;


	
	ProposalCreateBindingForAssistedFactory(GuiceModule guiceModule, IType factoryType, IType modelType) {
		super();
		this.factoryType = factoryType;
		this.modelType = modelType;
		this.guiceModule = guiceModule;
	}

	public String getModelTypeName() {
		return modelType.getElementName();
	}

	public String getFactoryTypeName() {
		return factoryType.getElementName();
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
		return "Create factory binding in module '"
				+ guiceModule.getTypeName()
				+ "'";
	}

	@Override
	/**
	 * Ausfuehrliche Beschreibung der Aktion, taucht in einem Fenster rechts
	 * neben der QuickfixProposal-Auswahl auf!
	 */
	public String getAdditionalProposalInfo() {
		return "Create factory binding for '"
				+ getFactoryTypeName()
				+ "' in module '"
				+ guiceModule.getTypeName()
				+ "'";
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

	@Override
	public void apply(IDocument document) {

		ICompilationUnit compilationUnit = IProjectResourceUtils.getICompilationUnit(guiceModule);
		CompilationUnit compilationUnitAstNode = ASTParserUtils.parseCompilationUnitAst3(compilationUnit);
		final Refactorator refactorator = Refactorator.create(compilationUnit, compilationUnitAstNode, compilationUnitAstNode.getAST());
		
		refactorator.addImport(GuiceConstants.GUICE_ASSISTEDINJECT_FACTORY_PROVIDER);
		refactorator.addImport(modelType);
		refactorator.addImport(factoryType);

		compilationUnitAstNode.accept(new ASTVisitor() {

			@SuppressWarnings("synthetic-access")
			@Override
			public boolean visit(MethodDeclaration method) {

				SimpleName simpleName = method.getName();
				String methodname = simpleName.toString();

				if (methodname.equals("configure")) {
					GuiceAssistedInjectFactoryBinding binding = new GuiceAssistedInjectFactoryBinding();
					String bindingStatementAsString = binding.generate(ProposalCreateBindingForAssistedFactory.this);
					refactorator.addAsLastStatementInMethod(method, bindingStatementAsString);
					
					return false; /* Stop processing the child nodes */
				}

				return super.visit(method);
			}

		});

		refactorator.refactor(null);

	}

	@Override
	public int getRelevance() {
		return 0;
	}

}
