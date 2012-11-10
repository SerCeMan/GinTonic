package de.jaculon.egap.junit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.jaculon.egap.utils.StringUtils;

public class MyPackage {

	private List<String> packageParts;

	public MyPackage() {
		this.packageParts = Collections.emptyList();
	}

	public MyPackage(String... packageParts) {
		super();
		this.packageParts = Arrays.asList(packageParts);
	}

	public MyPackage(List<String> packageParts) {
		super();
		this.packageParts = packageParts;
	}

	/**
	 * Returns the qualified name of the package. The qualified name is empty
	 * for an unnamed package.
	 *
	 * <h1>Example:</h1>
	 * <pre>
	 * "java.util"
	 * "de.jaculon.egap"
	 * </pre>
	 *
	 */
	public String getQualifiedName() {
		return StringUtils.join('.', packageParts);
	}

	/**
	 * Returns the parts that make up the source folder:
	 *
	 * <h1>Example:</h1>
	 *
	 * <pre>
	 * ["de","jaculon", "egap"]
	 * ["java","util"]
	 * </pre>
	 */
	public List<String> getPackageParts() {
		return packageParts;
	}

	@Override
	public String toString() {
		return getQualifiedName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((packageParts == null) ? 0 : packageParts.hashCode());
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
		MyPackage other = (MyPackage) obj;
		if (packageParts == null) {
			if (other.packageParts != null)
				return false;
		}
		else if (!packageParts.equals(other.packageParts))
			return false;
		return true;
	}

}
