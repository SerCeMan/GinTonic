package de.jaculon.egap.junit;

import java.util.List;

public interface ITypeNameResolver {

	public List<String> getPossibleTypeNamesFor(String compilationUnitName);

}
