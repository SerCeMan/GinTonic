package egap.utils;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * A project resource is a leightweight reference to a source code location. It is 
 * currently used to navigate from one source code location to another.
 * 
 * @author tmajunke
 */
public interface IProjectResource {

	/**
	 * The name of the project the type belongs to.
	 */
	String getProjectName();

	/**
	 * Returns the path to the src folder. Most likely this is ["src"] but
	 * maven projects use ["src", "main", "java"].
	 * 
	 * @return the path to the src folder.
	 */
	List<String> getPathToSrcFolder();

	/**
	 * Returns the package as list of strings (e.g ["java","lang"]).
	 * 
	 * @return the package as list of strings.
	 */
	List<String> getPackage();

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
	Integer getStartPosition();

	/**
	 * Returns the selection length like in {@link ASTNode#getLength()} or null
	 * if there is none.
	 */
	Integer getLength();
}
