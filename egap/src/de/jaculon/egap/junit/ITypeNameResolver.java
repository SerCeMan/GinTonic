package de.jaculon.egap.junit;

import java.util.List;

public interface ITypeNameResolver {

	/**
	 * Returns a list of type names for the given type name.
	 */
	public List<String> getTypeNamesFor(String typeName);

}
