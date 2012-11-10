package de.jaculon.egap.junit;

import java.util.Arrays;
import java.util.List;

import de.jaculon.egap.utils.StringUtils;

public class MySourceFolder {

	private List<String> sourceFolderParts;

	public MySourceFolder(String... sourceFolderParts) {
		super();
		this.sourceFolderParts = Arrays.asList(sourceFolderParts);
	}

	/**
	 * Returns the qualified name of the source folder.
	 *
	 * <h1>Example:</h1>
	 *
	 * <pre>
	 * "src"
	 * "src/main/java"
	 * </pre>
	 */
	public String getQualifiedName() {
		return StringUtils.join('/', sourceFolderParts);
	}

	/**
	 * Returns the parts that make up the source folder:
	 *
	 * <h1>Example:</h1>
	 *
	 * <pre>
	 * ["src"]
	 * ["src","main","java"]
	 * </pre>
	 */
	public List<String> getSourceFolderParts() {
		return sourceFolderParts;
	}

	@Override
	public String toString() {
		return getQualifiedName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((sourceFolderParts == null) ? 0
						: sourceFolderParts.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MySourceFolder other = (MySourceFolder) obj;
		if (sourceFolderParts == null) {
			if (other.sourceFolderParts != null)
				return false;
		}
		else if (!sourceFolderParts.equals(other.sourceFolderParts))
			return false;
		return true;
	}

}
