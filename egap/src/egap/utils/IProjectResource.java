package egap.utils;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * A java resource located inside a src folder of an eclipse project.
 * 
 * @author tmajunke
 */
public interface IProjectResource {

	/**
	 * The name of the project the type belongs to.
	 */
	String getProjectName();

	/**
	 * Returns the name components to the src folder.
	 * 
	 * @return the name components to the src folder.
	 */
	List<String> getSrcFolderPathComponents();

	/**
	 * Returns the package parts (e.g ["java","lang"]).
	 * 
	 * @return the package parts.
	 */
	List<String> getPackageNameComponents();

	/**
	 * The package name fully qualified (e.g java.lang). Is empty for the
	 * default package.
	 */
	String getPackageFullyQualified();

	/**
	 * Returns the simple name of the java type (e.g for {@link Collection} it
	 * would be Collection).
	 */
	String getTypeName();

	/**
	 * Returns the fully qualified type name (e.g for {@link Collection} it
	 * would be java.util.Collection).
	 */
	String getTypeNameFullyQualified();

	/**
	 * Returns the start position like in {@link ASTNode#getStartPosition()} or
	 * null if there is none.
	 */
	public Integer getStartPosition();

	/**
	 * Returns the selection length like in {@link ASTNode#getLength()} or null
	 * if there is none.
	 */
	public Integer getLength();
}
