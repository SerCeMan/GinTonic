package de.jaculon.egap.junit;

import java.util.List;

public interface ITypeNameResolver {

	/**
	 * Returns all possible names for the given type.
	 */
	public List<String> getPossibleTypeNamesFor(String typeName);

}
