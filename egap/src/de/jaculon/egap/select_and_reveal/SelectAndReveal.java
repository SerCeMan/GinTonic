package de.jaculon.egap.select_and_reveal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import de.jaculon.egap.EgapPlugin;
import de.jaculon.egap.utils.ASTParserUtils;
import de.jaculon.egap.utils.ICompilationUnitUtils;
import de.jaculon.egap.utils.StringUtils;

public class SelectAndReveal {

    /**
     * Selects and reveals the given field of the primary type of the given
     * {@link ICompilationUnit}.
     * 
     * @param compilationUnit
     *            the compilation unit where to access the field
     * @param fieldName
     *            the field to reveal
     * @throws JavaModelException
     *             if the field does not exist or if an exception occurs while
     *             accessing its corresponding resource.
     */
    public static void selectAndRevealField(ICompilationUnit compilationUnit, String fieldName)
            throws JavaModelException {
        IType primaryType = compilationUnit.findPrimaryType();
        IField field = primaryType.getField(fieldName);
        ISourceRange fieldRange = field.getNameRange();

        IResource resource = compilationUnit.getResource();
        SelectAndReveal.selectAndReveal((IFile) resource, fieldRange.getOffset(), 0);
    }

    /**
     * Selects and reveals the given field of the primary type of the given
     * {@link ICompilationUnit}.
     * 
     * @param compilationUnit
     *            the compilation unit where to access the field
     * @param fieldName
     *            the field to reveal
     * @throws JavaModelException
     *             if the field does not exist or if an exception occurs while
     *             accessing its corresponding resource.
     */
    public static void selectAndRevealType(String typeName, IProject project) {
        IJavaProject javaProject = JavaCore.create(project);
        IType type;
        try {
            typeName = StringUtils.removeGenerics(typeName);
            type = javaProject.findType(typeName);
            ISourceRange typeRange = type.getNameRange();
            IResource resource = type.getResource();
            if(resource == null) {
                EgapPlugin.logInfo("Source file for " + typeName + " not found");
                return;
            }
            SelectAndReveal.selectAndReveal((IFile) resource, typeRange.getOffset(), 0);
        } catch (JavaModelException e) {
            throw new RuntimeException("Error trying select type: ", e);
        }
    }

    public static int selectAndRevealParamOfMethod(ICompilationUnit iCompilationUnit, final String methodName,
            final String paramName) {
        CompilationUnit ast3 = ASTParserUtils.parseCompilationUnitAst3(iCompilationUnit);

        final List<SimpleName> simpleNames = new ArrayList<SimpleName>();
        ast3.accept(new ASTVisitor() {

            @SuppressWarnings("synthetic-access")
            @Override
            public boolean visit(MethodDeclaration method) {

                SimpleName simpleName = method.getName();
                String methodname = simpleName.getIdentifier();

                if (methodname.equals(methodName)) {
                    method.accept(new ASTVisitor() {
                        @Override
                        public boolean visit(SimpleName simpleName) {
                            String identifier = simpleName.getIdentifier();
                            if (identifier.equals(paramName)) {
                                simpleNames.add(simpleName);
                            }
                            return true;
                        }
                    });
                    return true;
                }

                return true;
            }

        });

        IResource resource = iCompilationUnit.getResource();
        SimpleName simpleName = simpleNames.get(0);
        int startPosition = simpleName.getStartPosition();
        SelectAndReveal.selectAndReveal((IFile) resource, startPosition, 0);
        return startPosition;
    }

    /**
     * Opens an editor with the given compilationUnit and sets the cursor on the
     * primary type.
     * 
     * @param iCompilationUnit
     *            the compilationUnit. May not be null.
     */
    public static void selectAndRevealPrimaryType(ICompilationUnit iCompilationUnit) {
        IResource resource = iCompilationUnit.getResource();

        if (resource instanceof IFile) {
            IFile file = (IFile) resource;
            Integer offsetOfTopLevelType = ICompilationUnitUtils.getStartPositionOfTopLevelType(iCompilationUnit);
            SelectAndReveal.selectAndReveal(file, offsetOfTopLevelType, 0);
        }
    }

    public static void selectAndReveal(IFile srcFile, int offset, int length) {
        try {
            IWorkbench workbench = PlatformUI.getWorkbench();
            IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
            IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
            ITextEditor editorPart = (ITextEditor) IDE.openEditor(activePage, srcFile, true);
            editorPart.selectAndReveal(offset, length);
        } catch (PartInitException pie) {
            EgapPlugin.logException(pie);
        }
    }

    public static void selectAndReveal(IFile srcFile, int offset) {
        selectAndReveal(srcFile, offset, 0);
    }

}
