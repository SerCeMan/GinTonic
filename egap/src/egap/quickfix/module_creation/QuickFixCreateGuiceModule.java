package egap.quickfix.module_creation;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

import egap.EgapPlugin;
import egap.quickfix.AbstractEgapQuickFix;
import egap.utils.ICompilationUnitUtils;
import egap.utils.ITypeBindingUtils;

/**
 * Enables the user to create a new guice module derived from a java class
 * (let's call it the source class).
 * 
 * <h5>How is the quick fix activated?</h5>
 * 
 * The quick fix is triggered if
 * 
 * <ul>
 * 
 * <li>the covering node is on a class declaration and</li>
 * <li>a guice module with the same name does not exist and</li>
 * <li>the source class is not a guice module</li>
 * 
 * </ul>
 * 
 * <pre>
 * <code>
 * public class Person {
 * </code>
 * </pre>
 * 
 * <h5>What name do you give the new guice module?</h5>
 * 
 * We simply append 'Module' to the source class name:
 * 
 * <pre>
 * <code>
 * Person => PersonModule
 * X => XModule
 * </code>
 * </pre>
 * 
 * 
 * <h5>Where is the guice module created?</h5>
 * 
 * In the same package as the source class.
 * 
 * @author tmajunke
 */
public class QuickFixCreateGuiceModule extends AbstractEgapQuickFix {

	@Override
	public void addProposals(IInvocationContext context,
			List<IJavaCompletionProposal> proposals) throws CoreException {
		ASTNode coveringNode = context.getCoveringNode();
		ASTNode parentNode = coveringNode.getParent();

		if (!(parentNode instanceof TypeDeclaration)) {
			return;
		}
		TypeDeclaration typeDecl = (TypeDeclaration) parentNode;
		ITypeBinding typeDeclBinding = typeDecl.resolveBinding();

		/* Make sure the source class is not a guice module! */
		boolean isGuiceModule = ITypeBindingUtils.isGuiceModuleType(typeDeclBinding);
		if (isGuiceModule) {
			return;
		}

		ICompilationUnit compilationUnit = context.getCompilationUnit();

		/*
		 * Den Klassennamen der Compilation Unit ermitteln.
		 * 
		 * Wir leiten den Namen des Guice Moduls aus der Compilation Unit ab
		 * (z.B Person => PersonModule)
		 */
		IType targetClassType = compilationUnit.findPrimaryType();
		String targetClassName = targetClassType.getElementName();
		String guiceModuleName = targetClassName + "Module";

		/*
		 * Dann benötigen wir noch das package um das Module zu erzeugen.
		 */
		IPackageFragment packageFragment = (IPackageFragment) compilationUnit.getParent();
		ICompilationUnit guiceModuleCompilationUnit = packageFragment.getCompilationUnit(guiceModuleName
				+ ICompilationUnitUtils.JAVA_EXTENSION);

		/*
		 * Falls wir kein existierendes Module mit gleichem Namen finden, dann
		 * zeigen wir einen QuickFix an, mit der eben dieses erzeugt werden
		 * kann.
		 */
		if (!guiceModuleCompilationUnit.exists()) {
			ProposalCreateGuiceModule createGuiceModule = new ProposalCreateGuiceModule(
					packageFragment,
					guiceModuleName);
			proposals.add(createGuiceModule);
		}
	}

	@Override
	public String getPreferencesDisplayName() {
		return "Create Guice Module";
	}

	@Override
	public String getEnabledStateID() {
		return EgapPlugin.ID_QUICKFIXCREATEGUICEMODULE_ENABLED_STATE_ID;
	}

}
