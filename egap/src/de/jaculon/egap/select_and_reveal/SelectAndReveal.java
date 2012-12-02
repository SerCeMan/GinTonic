package de.jaculon.egap.select_and_reveal;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import de.jaculon.egap.utils.IFileUtils;

public class SelectAndReveal {

	/**
	 * Selects and reveals the given field of the primary type of the given
	 * {@link ICompilationUnit}.
	 * 
	 * @param compilationUnit the compilation unit where to access the field
	 * @param fieldName the field to reveal
	 * @throws JavaModelException if the field does not exist or if an exception
	 *             occurs while accessing its corresponding resource.
	 */
	public static void selectAndRevealField(ICompilationUnit compilationUnit,
			String fieldName) throws JavaModelException {
		IType primaryType = compilationUnit.findPrimaryType();
		IField field = primaryType.getField(fieldName);
		ISourceRange fieldRange = field.getNameRange();

		IResource resource = compilationUnit.getResource();
		IFileUtils.selectAndRevealInEditor(
				(IFile) resource,
				fieldRange.getOffset(),
				0);
	}

}
