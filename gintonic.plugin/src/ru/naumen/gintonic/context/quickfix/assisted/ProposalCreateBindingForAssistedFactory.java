package ru.naumen.gintonic.context.quickfix.assisted;

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

import ru.naumen.gintonic.context.refactor.Refactorator;
import ru.naumen.gintonic.guice.GuiceConstants;
import ru.naumen.gintonic.guice.GuiceModule;
import ru.naumen.gintonic.plugin.icons.Icons;
import ru.naumen.gintonic.templates.GuiceAssistedInjectFactoryBinding;
import ru.naumen.gintonic.utils.ASTParserUtils;


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
		return Icons.ginTonicIconCreate;
	}

	@Override
	/**
	 * Brief description of the action appears in the QuickfixProposal selection!
	 */
	public String getDisplayString() {
		return "Create factory binding in module '"
				+ guiceModule.getPrimaryTypeName()
				+ "'";
	}

	@Override
	/**
	 * Detailed description of the action that appears in the box next to the selection QuickfixProposal!
	 */
	public String getAdditionalProposalInfo() {
		return "Create factory binding for '"
				+ getFactoryTypeName()
				+ "' in module '"
				+ guiceModule.getPrimaryTypeName()
				+ "'";
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

	@Override
	public void apply(IDocument document) {

		ICompilationUnit compilationUnit = guiceModule.getSourceCodeReference().resolveICompilationUnit();
		CompilationUnit compilationUnitAstNode = ASTParserUtils.parseCompilationUnitAst3(compilationUnit);
		final Refactorator refactorator = Refactorator.create(compilationUnit, compilationUnitAstNode, compilationUnitAstNode.getAST());
		
		refactorator.addImport(GuiceConstants.ASSISTEDINJECT_FACTORY_PROVIDER);
		refactorator.addImport(modelType);
		refactorator.addImport(factoryType);

		compilationUnitAstNode.accept(new ASTVisitor() {

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
