package de.jaculon.egap.junit;

import java.util.List;

public interface TypeNameResolver {

	public List<String> getPossibleTypeNamesFor(String compilationUnitName);

}
