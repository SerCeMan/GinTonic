package ru.naumen.gintonic.utils;

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.dom.*;

import ru.naumen.gintonic.guice.GuiceConstants;

public class ASTNodeUtils {

    public static boolean isAssistedAnnotation(ASTNode node) {
        return isMarkerAnnotationAnnotatedWith(node, GuiceConstants.ANNOTATION_ASSISTED);
    }

    /**
     * Resolves the {@link ITypeBinding} for the Ast node.
     * 
     * @param coveringNode
     *            the ast node.
     * @return the type binding or null if the node does not represent a guice
     *         module.
     */
    public static ITypeBinding getTypeBindingIfGuiceModuleCovered(ASTNode node) {
        if (node instanceof Name) {
            Name name = (Name) node;
            ITypeBinding guiceModuleSource = name.resolveTypeBinding();
            boolean isGuiceModule = ITypeBindingUtils.isGuiceAbstractModuleType(guiceModuleSource);
            if (isGuiceModule) {
                return guiceModuleSource;
            }
        }
        return null;
    }

    /**
     * Resolves the {@link ITypeBinding} for the Ast node if the node is a
     * {@link Name} and the type is <b>not</b> a Guice module.
     * 
     * @param coveringNode
     *            the ast node.
     * @return the type binding or null if the node does represent a guice
     *         module.
     */
    public static ITypeBinding getTypeBindingIfNotAGuiceModule(ASTNode node) {
        if (node instanceof Name) {
            Name name = (Name) node;
            ITypeBinding guiceModuleSource = name.resolveTypeBinding();
            boolean isGuiceModule = ITypeBindingUtils.isGuiceAbstractModuleType(guiceModuleSource);
            if (!isGuiceModule) {
                return guiceModuleSource;
            }
        }
        return null;
    }

    public static AnnotationList getAnnotationList(List<ASTNode> modifiers) {
        List<Annotation> annotations = ListUtils.newArrayListWithCapacity(modifiers.size());

        for (ASTNode modifier : modifiers) {
            if (modifier instanceof Annotation) {
                annotations.add((Annotation) modifier);
            }
        }
        return new AnnotationList(annotations);
    }

    private static final class FindMethodByName extends ASTVisitor {
        private final String fieldAsSetter;
        private List<MethodDeclaration> methodDeclarations = ListUtils.newArrayList();

        private FindMethodByName(String fieldAsSetter) {
            super(false);
            this.fieldAsSetter = fieldAsSetter;
        }

        @Override
        public boolean visit(MethodDeclaration node) {
            SimpleName name = node.getName();
            String fullyQualifiedName = name.getFullyQualifiedName();
            if (fullyQualifiedName.equals(fieldAsSetter)) {
                methodDeclarations.add(node);
            }
            return false;
        }
    }

    /**
     * Returns true if the given node is a {@link MarkerAnnotation} and the
     * qualified name of its type is equal to the given one.
     * 
     * @param node
     *            the node
     * @param annotationFullyQualified
     *            the annotation name we compare
     * 
     * @return true if it is annotated with the given type otherwise false.
     */
    private static boolean isMarkerAnnotationAnnotatedWith(ASTNode node, String annotationFullyQualified) {
        if (node instanceof MarkerAnnotation) {
            MarkerAnnotation markerAnnotation = (MarkerAnnotation) node;
            ITypeBinding typeBinding = markerAnnotation.resolveTypeBinding();

            String qualifiedName = typeBinding.getQualifiedName();
            if (qualifiedName.equals(annotationFullyQualified)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the given node is an identifier of a {@link FieldDeclaration}.
     * If it is then it is returned, otherwise null is returned.
     * 
     * @param astNode
     *            the node
     * @return the field declaration or null.
     */
    public static FieldDeclaration getFieldDeclaration(ASTNode astNode) {
        if (!(astNode instanceof Name)) {
            return null;
        }

        Name name = (Name) astNode;
        ASTNode parentNode = name.getParent();

        if (!(parentNode.getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT)) {
            return null;
        }

        VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) parentNode;
        ASTNode parentParentNode = declarationFragment.getParent();

        if (parentParentNode instanceof FieldDeclaration) {
            FieldDeclaration fieldDeclaration = (FieldDeclaration) parentParentNode;
            return fieldDeclaration;

        }

        return null;
    }

    /**
     * Returns the parent of the given node if it is a {@link TypeDeclaration},
     * otherwise null.
     */
    public static TypeDeclaration getTypeDeclaration(ASTNode astNode) {
        ASTNode parentNode = astNode.getParent();
        if (parentNode instanceof TypeDeclaration) {
            TypeDeclaration typeDeclaration = (TypeDeclaration) parentNode;
            return typeDeclaration;
        }
        return null;
    }

    public static List<MethodDeclaration> getMethodByName(CompilationUnit astRoot, final String methodName) {

        FindMethodByName methodByName = new FindMethodByName(methodName);
        astRoot.accept(methodByName);
        List<MethodDeclaration> methodDeclarations = methodByName.methodDeclarations;

        return methodDeclarations;
    }

    public static MethodDeclaration getMethodByNameExpectSingleMethod(CompilationUnit astRoot, final String methodName) {
        List<MethodDeclaration> methodByName = getMethodByName(astRoot, methodName);
        if (methodByName.size() > 0) {
            MethodDeclaration methodDeclaration = methodByName.get(0);
            return methodDeclaration;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static boolean isProviderMethod(ASTNode astNode) {
        if (astNode instanceof MethodDeclaration) {
            MethodDeclaration methodDeclaration = (MethodDeclaration) astNode;

            List<ASTNode> modifiers = methodDeclaration.modifiers();
            AnnotationList markerAnnotationList = ASTNodeUtils.getAnnotationList(modifiers);
            if (markerAnnotationList.containsProvidesAnnotation()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param simpleName
     *            the not <code>null</code> {@link SimpleName}
     * 
     * @return {@link IVariableBinding} or <code>null</code> for given
     *         {@link SimpleName}.
     */
    public static IVariableBinding getVariableBinding(ASTNode node) {
        Assert.isNotNull(node);
        // try to get binding from property (copy of binding added by
        // DesignerAST)
        {
            IVariableBinding binding = (IVariableBinding) node.getProperty("VARIABLE_BINDING");
            if (binding != null) {
                return binding;
            }
        }
        // VariableDeclaration
        if (node instanceof VariableDeclaration) {
            VariableDeclaration variableDeclaration = (VariableDeclaration) node;
            IVariableBinding binding = variableDeclaration.resolveBinding();
            if (binding != null) {
                return binding;
            }
        }
        // check for SimpleName
        if (node instanceof SimpleName) {
            SimpleName simpleName = (SimpleName) node;
            // get standard binding
            {
                IBinding binding = simpleName.resolveBinding();
                if (binding instanceof IVariableBinding) {
                    return (IVariableBinding) binding;
                }
            }
        }
        // check for FieldAccess
        if (node instanceof FieldAccess) {
            FieldAccess fieldAccess = (FieldAccess) node;
            return fieldAccess.resolveFieldBinding();
        }
        // not a variable
        return null;
    }

}
