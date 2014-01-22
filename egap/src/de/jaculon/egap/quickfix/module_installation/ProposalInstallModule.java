package de.jaculon.egap.quickfix.module_installation;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import de.jaculon.egap.guice.GuiceModule;
import de.jaculon.egap.icons.Icons;
import de.jaculon.egap.refactor.Refactorator;
import de.jaculon.egap.templates.InstallModuleTemplate;
import de.jaculon.egap.utils.ASTParserUtils;

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

    public ProposalInstallModule(ITypeBinding guiceModuleSource, GuiceModule guiceModuleTarget) {
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

    /**
     * Brief description of the action appears in the QuickfixProposal selection!
     */
    @Override
    public String getDisplayString() {
        return "Install '" + guiceModuleSource.getName() + "' in '" + guiceModuleTarget.getPrimaryTypeName() + "'";
    }

    /**
     * Detailed description of the action, immersed in a window right next to the QuickfixProposal selection on!
     */
    @Override
    public String getAdditionalProposalInfo() {
        return null;
    }

    @Override
    public IContextInformation getContextInformation() {
        return null;
    }

    @Override
    public void apply(IDocument document) {
        ICompilationUnit compilationUnit = guiceModuleTarget.getSourceCodeReference().resolveICompilationUnit();
        CompilationUnit compilationUnitAstNode = ASTParserUtils.parseCompilationUnitAst3(compilationUnit);
        Refactorator refactorator = Refactorator.create(compilationUnit, compilationUnitAstNode,
                compilationUnitAstNode.getAST());
        refactorator.addImport(guiceModuleSource.getQualifiedName());
        compilationUnitAstNode.accept(new AddInstallModuleStatement(refactorator));
        refactorator.refactor(null);
    }

    @Override
    public int getRelevance() {
        return 0;
    }

}